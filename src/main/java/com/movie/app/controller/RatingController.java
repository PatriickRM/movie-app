package com.movie.app.controller;

import com.movie.app.model.dto.common.ApiResponse;
import com.movie.app.model.dto.common.PageResponse;
import com.movie.app.model.dto.rating.request.AddRatingRequest;
import com.movie.app.model.dto.rating.request.UpdateRatingRequest;
import com.movie.app.model.dto.rating.response.RatingResponse;
import com.movie.app.security.UserPrincipal;
import com.movie.app.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Slf4j
public class RatingController {
    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<ApiResponse<RatingResponse>> addOrUpdateRating(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                         @Valid @RequestBody AddRatingRequest request) {

        log.info("POST /api/ratings - User: {}, Movie: {}", userPrincipal.getId(), request.getMovieId());

        RatingResponse rating = ratingService.addOrUpdateRating(userPrincipal.getId(), request);
        ApiResponse<RatingResponse> response = ApiResponse.<RatingResponse>builder()
                .success(true)
                .message("Calificación guardada exitosamente")
                .data(rating)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    //Obtener calificaciones del usuario con paginación
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RatingResponse>>> getUserRatings(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                                    @RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/ratings - User: {}, Page: {}, Size: {}", userPrincipal.getId(), page, size);

        PageResponse<RatingResponse> ratings = ratingService.getUserRatings(userPrincipal.getId(), page, size);
        ApiResponse<PageResponse<RatingResponse>> response =
                ApiResponse.<PageResponse<RatingResponse>>builder()
                        .success(true)
                        .data(ratings)
                        .build();

        return ResponseEntity.ok(response);
    }

    //Obtener calificación de una pelicula específica
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse<RatingResponse>> getRatingByMovie(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                        @PathVariable Integer movieId ){
        log.info("GET /api/ratings/movie/{} - User: {}", movieId, userPrincipal.getId());

        RatingResponse rating = ratingService.getRatingByMovie(userPrincipal.getId(), movieId);

        ApiResponse<RatingResponse> response = ApiResponse.<RatingResponse>builder()
                .success(true)
                .data(rating)
                .build();

        return ResponseEntity.ok(response);
    }


    //Verificar si el usuario ya califico una película
    @GetMapping("/check/{movieId}")
    public ResponseEntity<ApiResponse<Boolean>> hasRated(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer movieId) {
        log.info("GET /api/ratings/check/{} - User: {}", movieId, userPrincipal.getId());

        boolean hasRated = ratingService.hasRated(userPrincipal.getId(), movieId);
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(true)
                .data(hasRated)
                .build();

        return ResponseEntity.ok(response);
    }

    //Obtener promedio de calificaciones del usuario
    @GetMapping("/average")
    public ResponseEntity<ApiResponse<Double>> getUserAverageRating(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/ratings/average - User: {}", userPrincipal.getId());

        Double average = ratingService.getUserAverageRating(userPrincipal.getId());
        ApiResponse<Double> response = ApiResponse.<Double>builder()
                .success(true)
                .data(average)
                .build();

        return ResponseEntity.ok(response);
    }

    //Obtener películas mejor calificadas por el usuario
    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getTopRatedMovies(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("GET /api/ratings/top - User: {}, Limit: {}", userPrincipal.getId(), limit);

        List<RatingResponse> topRated = ratingService.getTopRatedMovies(userPrincipal.getId(), limit);

        ApiResponse<List<RatingResponse>> response = ApiResponse.<List<RatingResponse>>builder()
                .success(true)
                .data(topRated)
                .build();

        return ResponseEntity.ok(response);
    }

    //Obtener IDs de películas calificadas
    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<List<Integer>>> getRatedMovieIds(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/ratings/ids - User: {}", userPrincipal.getId());

        List<Integer> movieIds = ratingService.getRatedMovieIds(userPrincipal.getId());
        ApiResponse<List<Integer>> response = ApiResponse.<List<Integer>>builder()
                .success(true)
                .data(movieIds)
                .build();

        return ResponseEntity.ok(response);
    }


    //Actualizar calificación existente
    @PutMapping("/{movieId}")
    public ResponseEntity<ApiResponse<RatingResponse>> updateRating(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer movieId,
            @Valid @RequestBody UpdateRatingRequest request
    ) {
        log.info("PUT /api/ratings/{} - User: {}", movieId, userPrincipal.getId());

        RatingResponse rating = ratingService.updateRating(userPrincipal.getId(), movieId, request);

        ApiResponse<RatingResponse> response = ApiResponse.<RatingResponse>builder()
                .success(true)
                .message("Calificación actualizada exitosamente")
                .data(rating)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<ApiResponse<Void>> deleteRating(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer movieId) {
        log.info("DELETE /api/ratings/{} - User: {}", movieId, userPrincipal.getId());

        ratingService.deleteRating(userPrincipal.getId(), movieId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Calificación eliminada exitosamente")
                .build();

        return ResponseEntity.ok(response);
    }
}