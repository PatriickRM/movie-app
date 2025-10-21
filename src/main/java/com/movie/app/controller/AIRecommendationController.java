package com.movie.app.controller;

import com.movie.app.model.dto.ai.request.AIRecommendationRequest;
import com.movie.app.model.dto.ai.response.AIRecommendationResponse;
import com.movie.app.model.dto.auth.response.AILimitResponse;
import com.movie.app.model.dto.common.ApiResponse;
import com.movie.app.security.UserPrincipal;
import com.movie.app.service.AIRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIRecommendationController {
    private final AIRecommendationService aiRecommendationService;

    //Obtener recomendaciones de películas usando IA
    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse<AIRecommendationResponse>> getRecommendation(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                                   @Valid @RequestBody AIRecommendationRequest request) {

        log.info("POST /api/ai/recommend - User: {}, Prompt: {}", userPrincipal.getId(), request.getPrompt());

        AIRecommendationResponse recommendation = aiRecommendationService.getRecommendation(userPrincipal.getId(), request);
        ApiResponse<AIRecommendationResponse> response =
                ApiResponse.<AIRecommendationResponse>builder()
                        .success(true)
                        .message("Recomendaciones generadas exitosamente")
                        .data(recommendation)
                        .build();

        return ResponseEntity.ok(response);
    }


    //Verificar si el usuario puede hacer una request de IA
    @GetMapping("/can-request")
    public ResponseEntity<ApiResponse<AILimitResponse>> canRequestAI(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/ai/can-request - User: {}", userPrincipal.getId());

        AILimitResponse limitInfo = aiRecommendationService.canRequestAI(userPrincipal.getId());
        ApiResponse<AILimitResponse> response = ApiResponse.<AILimitResponse>builder()
                .success(true)
                .data(limitInfo)
                .build();

        return ResponseEntity.ok(response);
    }
}