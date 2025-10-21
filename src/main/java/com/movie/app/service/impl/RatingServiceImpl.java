package com.movie.app.service.impl;

import com.movie.app.model.dto.common.PageResponse;
import com.movie.app.model.dto.rating.request.AddRatingRequest;
import com.movie.app.model.dto.rating.request.UpdateRatingRequest;
import com.movie.app.model.dto.rating.response.RatingResponse;
import com.movie.app.service.RatingService;
import com.movie.app.exception.ResourceNotFoundException;
import com.movie.app.model.entity.Rating;
import com.movie.app.model.entity.User;
import com.movie.app.repository.RatingRepository;
import com.movie.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    //Agregar o actualizar calificación
    @Transactional
    public RatingResponse addOrUpdateRating(Long userId, AddRatingRequest request) {
        log.info("Agregando/actualizando calificación para película {} del usuario {}",
                request.getMovieId(), userId);

        User user = findUserById(userId);

        // Buscar si ya existe una calificación
        Rating rating = ratingRepository.findByUserIdAndMovieId(userId, request.getMovieId())
                .orElse(null);

        if (rating == null) {
            // Crear nueva calificación
            rating = Rating.builder()
                    .user(user)
                    .movieId(request.getMovieId())
                    .rating(request.getRating())
                    .review(request.getReview())
                    .build();

            log.info("Creando nueva calificación");
        } else {
            // Actualizar calificación existente
            rating.setRating(request.getRating());
            rating.setReview(request.getReview());

            log.info("Actualizando calificación existente");
        }

        rating = ratingRepository.save(rating);

        log.info("Calificación guardada exitosamente para película {}", request.getMovieId());

        return mapToRatingResponse(rating);
    }

    //Actualizar calificación existente
    @Transactional
    public RatingResponse updateRating(Long userId, Integer movieId, UpdateRatingRequest request) {
        log.info("Actualizando calificación para película {} del usuario {}", movieId, userId);

        Rating rating = ratingRepository.findByUserIdAndMovieId(userId, movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Calificación no encontrada"));

        rating.setRating(request.getRating());
        rating.setReview(request.getReview());

        rating = ratingRepository.save(rating);

        log.info("Calificación actualizada exitosamente");

        return mapToRatingResponse(rating);
    }

    //Eliminar calificación
    @Transactional
    public void deleteRating(Long userId, Integer movieId) {
        log.info("Eliminando calificación de película {} del usuario {}", movieId, userId);

        if (!ratingRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw new ResourceNotFoundException("Calificación no encontrada");
        }

        ratingRepository.deleteByUserIdAndMovieId(userId, movieId);

        log.info("Calificación eliminada exitosamente");
    }

    //Obtener calificaciones de usuario por paginación
    @Transactional(readOnly = true)
    public PageResponse<RatingResponse> getUserRatings(Long userId, int page, int size) {
        log.info("Obteniendo calificaciones del usuario {} - Página: {}, Tamaño: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("watchedAt").descending());
        Page<Rating> ratingsPage = ratingRepository.findByUserId(userId, pageable);

        List<RatingResponse> ratings = ratingsPage.getContent().stream()
                .map(this::mapToRatingResponse)
                .collect(Collectors.toList());

        return PageResponse.<RatingResponse>builder()
                .content(ratings)
                .page(page)
                .size(size)
                .totalElements(ratingsPage.getTotalElements())
                .totalPages(ratingsPage.getTotalPages())
                .isLast(ratingsPage.isLast())
                .build();
    }

    //Obtener calificación de una película especifica
    @Transactional(readOnly = true)
    public RatingResponse getRatingByMovie(Long userId, Integer movieId) {
        Rating rating = ratingRepository.findByUserIdAndMovieId(userId, movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Calificación no encontrada"));

        return mapToRatingResponse(rating);
    }


    //Verificar si el usuario ya calificó una película
    @Transactional(readOnly = true)
    public boolean hasRated(Long userId, Integer movieId) {
        return ratingRepository.existsByUserIdAndMovieId(userId, movieId);
    }


    //Obtener promedio de calificaciones del usuario
    @Transactional(readOnly = true)
    public Double getUserAverageRating(Long userId) {
        Double average = ratingRepository.getAverageRatingByUserId(userId);
        return average != null ? average : 0.0;
    }

    //Obtener películas mejor calificadas por el usuario
    @Transactional(readOnly = true)
    public List<RatingResponse> getTopRatedMovies(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return ratingRepository.findTopRatedByUserId(userId, pageable).stream()
                .map(this::mapToRatingResponse)
                .collect(Collectors.toList());
    }


    //Obtener IDs de películas calificadas
    @Transactional(readOnly = true)
    public List<Integer> getRatedMovieIds(Long userId) {
        return ratingRepository.findMovieIdsByUserId(userId);
    }

    //Buscar usuario por ID
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
    }

   //Mapear Rating a response
    private RatingResponse mapToRatingResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .movieId(rating.getMovieId())
                .rating(rating.getRating())
                .review(rating.getReview())
                .watchedAt(rating.getWatchedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}