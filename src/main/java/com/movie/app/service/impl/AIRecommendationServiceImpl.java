package com.movie.app.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.movie.app.exception.BadRequestException;
import com.movie.app.exception.ResourceNotFoundException;
import com.movie.app.model.dto.ai.request.AIRecommendationRequest;
import com.movie.app.model.dto.ai.response.AIRecommendationResponse;
import com.movie.app.model.dto.ai.response.MovieRecommendation;
import com.movie.app.model.dto.auth.response.AILimitResponse;
import com.movie.app.model.entity.AIInteraction;
import com.movie.app.model.entity.User;
import com.movie.app.model.entity.UserPlan;
import com.movie.app.repository.AIInteractionRepository;
import com.movie.app.repository.FavoriteRepository;
import com.movie.app.repository.RatingRepository;
import com.movie.app.repository.UserRepository;
import com.movie.app.service.AIRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIRecommendationServiceImpl implements AIRecommendationService {
    private final AIInteractionRepository aiInteractionRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final RatingRepository ratingRepository;
    private final OkHttpClient httpClient;
    private final Gson gson;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Value("${gemini.model}")
    private String geminiModel;

    @Value("${gemini.base-url}")
    private String geminiBaseUrl;

    @Value("${tmdb.api-key}")
    private String tmdbApiKey;

    //Obtener recomendación de IA
    @Transactional
    public AIRecommendationResponse getRecommendation(Long userId, AIRecommendationRequest request) {
        log.info("Obteniendo recomendación de IA para usuario {}", userId);

        User user = findUserById(userId);

        // Verificar límite de requests (solo para usuarios FREE)
        if (user.getPlan() == UserPlan.FREE) {
            checkAndUpdateAILimit(user);
        }

        // Construir contexto del usuario
        String userContext = buildUserContext(userId, request.getIncludeUserHistory());
        // Construir prompt completo
        String fullPrompt = buildPrompt(request.getPrompt(), userContext, request.getMaxRecommendations());
        // Llamar a Gemini API
        String aiResponse = callGeminiAPI(fullPrompt);

        // Parsear respuesta
        List<MovieRecommendation> recommendations = parseRecommendations(aiResponse);

        // Guardar interacción
        saveInteraction(user, request.getPrompt(), aiResponse);

        // Calcular requests restantes
        Integer requestsRemaining = null;
        if (user.getPlan() == UserPlan.FREE) {
            requestsRemaining = 1 - user.getAiRequestsToday();
        }

        log.info("Recomendación generada exitosamente. {} películas recomendadas", recommendations.size());

        return AIRecommendationResponse.builder()
                .recommendations(recommendations)
                .explanation(extractExplanation(aiResponse))
                .requestsRemainingToday(requestsRemaining)
                .build();
    }

    //Verificar si el usuario puede hacer una request de IA
    @Transactional(readOnly = true)
    public AILimitResponse canRequestAI(Long userId) {
        User user = findUserById(userId);

        if (user.getPlan() == UserPlan.PREMIUM) {
            return AILimitResponse.builder()
                    .canRequest(true)
                    .requestsRemainingToday(null)
                    .isPremium(true)
                    .message("Requests ilimitados como usuario Premium")
                    .build();
        }

        // Verificar si necesita reset
        if (needsReset(user)) {
            return AILimitResponse.builder()
                    .canRequest(true)
                    .requestsRemainingToday(1)
                    .isPremium(false)
                    .message("Puedes hacer 1 request de IA hoy")
                    .build();
        }

        boolean canRequest = user.getAiRequestsToday() < 1;
        int remaining = Math.max(0, 1 - user.getAiRequestsToday());

        return AILimitResponse.builder()
                .canRequest(canRequest)
                .requestsRemainingToday(remaining)
                .isPremium(false)
                .message(canRequest ?
                        "Puedes hacer " + remaining + " request de IA hoy" :
                        "Has alcanzado el límite diario. Actualiza a Premium para requests ilimitados")
                .build();
    }

    //Verificar y actualizar límite de IA
    private void checkAndUpdateAILimit(User user) {
        //Reset si es un nuevo día
        if (needsReset(user)) {
            user.setAiRequestsToday(0);
            user.setAiLastReset(LocalDateTime.now());
        }

        //Verificar límite
        if (user.getAiRequestsToday() >= 1) {
            throw new BadRequestException(
                    "Has alcanzado el límite diario de 1 request de IA. " +
                            "Actualiza a Premium para requests ilimitados"
            );
        }

        // Incrementar contador
        user.setAiRequestsToday(user.getAiRequestsToday() + 1);
        userRepository.save(user);
    }


    //Verificar si necesita reset del contador diario
    private boolean needsReset(User user) {
        if (user.getAiLastReset() == null) {
            return true;
        }
        LocalDate lastReset = user.getAiLastReset().toLocalDate();
        LocalDate today = LocalDate.now();
        return lastReset.isBefore(today);
    }

    //Construir contexto del usuario
    private String buildUserContext(Long userId, Boolean includeHistory) {
        if (!includeHistory) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        context.append("\n\nCONTEXTO DEL USUARIO:\n");

        //Obtener favoritos
        List<Integer> favoriteIds = favoriteRepository.findMovieIdsByUserId(userId);
        if (!favoriteIds.isEmpty()) {
            context.append("Películas favoritas (IDs): ").append(favoriteIds).append("\n");
        }

        //Obtener ratings
        List<Integer> ratedIds = ratingRepository.findMovieIdsByUserId(userId);
        if (!ratedIds.isEmpty()) {
            context.append("Películas calificadas (IDs): ").append(ratedIds).append("\n");
        }

        return context.toString();
    }

    //Construir prompt completo
    private String buildPrompt(String userPrompt, String userContext, Integer maxRecommendations) {
        return String.format("""
            Eres un experto en cine y tu tarea es recomendar películas basadas en las preferencias del usuario.

            ⚠INSTRUCCIONES CRÍTICAS:
            - Devuelve **únicamente JSON válido**.
            - NO incluyas texto antes ni después del JSON.
            - NO uses explicaciones fuera del objeto JSON.

            Reglas:
            1. Recomienda exactamente %d películas.
            2. Incluye el ID real de TMDb (entero válido).
            3. Explica brevemente por qué recomiendas cada una.
            4. Usa películas populares y conocidas.
            5. Si no sabes el ID exacto, usa 0.

            Contexto del usuario (si existe):
            %s

            Solicitud del usuario:
            %s

            FORMATO DE RESPUESTA (OBLIGATORIO):
            {
              "recommendations": [
                {
                  "movieId": 550,
                  "title": "Fight Club",
                  "reason": "Razón de la recomendación"
                }
              ],
              "explanation": "Explicación general de las recomendaciones"
            }

            Devuelve **solo** el JSON anterior, sin ningún texto adicional.
            """,
                maxRecommendations, userContext, userPrompt
        );
    }



    //Llamar a Gemini API
    private String callGeminiAPI(String prompt) {
        try {
            // Construir URL
            String url = String.format("%s/%s:generateContent?key=%s",
                    geminiBaseUrl, geminiModel, geminiApiKey);

            //Construir requestbody
            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);

            // Configuración de generación
            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.7);
            generationConfig.addProperty("maxOutputTokens", 1000);
            requestBody.add("generationConfig", generationConfig);

            RequestBody body = RequestBody.create(
                    gson.toJson(requestBody),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            //Ejecutar request
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Error en Gemini API: {}", response.code());
                    throw new BadRequestException("Error al obtener recomendaciones de IA");
                }

                String responseBody = response.body().string();
                log.debug("Respuesta de Gemini API: {}", responseBody);

                // Parsear respuesta
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                return jsonResponse
                        .getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
            }

        } catch (IOException e) {
            log.error("Error al llamar a Gemini API", e);
            throw new BadRequestException("Error de conexión con el servicio de IA");
        }
    }

    //Parsear recomendaciones del response de Gemini
    //Parsear recomendaciones del response de Gemini
    private List<MovieRecommendation> parseRecommendations(String aiResponse) {
        try {
            // Limpiar response (remover markdown si existe)
            String cleanedResponse = aiResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            log.debug("Cleaned AI Response: {}", cleanedResponse);

            // 🔹 AQUI VIENE LA NUEVA PARTE: limpiar cualquier texto fuera del JSON
            if (!cleanedResponse.startsWith("{")) {
                int firstBrace = cleanedResponse.indexOf("{");
                int lastBrace = cleanedResponse.lastIndexOf("}");
                if (firstBrace != -1 && lastBrace != -1) {
                    cleanedResponse = cleanedResponse.substring(firstBrace, lastBrace + 1);
                }
            }

            // Ahora sí parseamos
            JsonObject jsonResponse = gson.fromJson(cleanedResponse, JsonObject.class);
            JsonArray recommendations = jsonResponse.getAsJsonArray("recommendations");

            List<MovieRecommendation> result = new ArrayList<>();

            for (int i = 0; i < recommendations.size(); i++) {
                JsonObject rec = recommendations.get(i).getAsJsonObject();

                int movieId = rec.get("movieId").getAsInt();
                String title = rec.get("title").getAsString();
                String reason = rec.get("reason").getAsString();

                // Verificar y corregir el ID con TMDb
                movieId = verifyMovieId(title, movieId);

                log.info("Parsed recommendation - ID: {}, Title: {}", movieId, title);

                result.add(MovieRecommendation.builder()
                        .movieId(movieId)
                        .title(title)
                        .reason(reason)
                        .build());
            }

            log.info("Total recommendations parsed: {}", result.size());
            return result;

        } catch (Exception e) {
            log.error("Error al parsear recomendaciones: {}", aiResponse, e);
            throw new BadRequestException("Error al procesar respuesta de IA: " + e.getMessage());
        }
    }


    //
    private int verifyMovieId(String title, int aiMovieId) {
        try {
            String url = String.format(
                    "https://api.themoviedb.org/3/search/movie?api_key=%s&query=%s&language=es-ES",
                    tmdbApiKey,
                    URLEncoder.encode(title, StandardCharsets.UTF_8)
            );

            Request request = new Request.Builder().url(url).build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                    JsonArray results = json.getAsJsonArray("results");

                    if (results != null && results.size() > 0) {
                        return results.get(0).getAsJsonObject().get("id").getAsInt(); //  ID real
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error verificando movieId para '{}': {}", title, e.getMessage());
        }

        // Si algo falla, usa el ID original que dio la IA
        return aiMovieId;
    }


    //Extraer explicación general
    private String extractExplanation(String aiResponse) {
        try {
            String cleanedResponse = aiResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonObject jsonResponse = gson.fromJson(cleanedResponse, JsonObject.class);
            return jsonResponse.get("explanation").getAsString();
        } catch (Exception e) {
            return "Recomendaciones basadas en tus preferencias";
        }
    }

    //Guardar interacción con IA
    private void saveInteraction(User user, String prompt, String response) {
        AIInteraction interaction = AIInteraction.builder()
                .user(user)
                .prompt(prompt)
                .response(response)
                .tokensUsed(null) // Gemini no proporciona esto directamente
                .build();

        aiInteractionRepository.save(interaction);
    }

   //Buscar usuario por id
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
    }
}
