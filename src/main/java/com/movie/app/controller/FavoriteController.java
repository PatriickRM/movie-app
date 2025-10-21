package com.movie.app.controller;

import com.movie.app.model.dto.common.ApiResponse;
import com.movie.app.model.dto.common.PageResponse;
import com.movie.app.model.dto.favorite.request.AddFavoriteRequest;
import com.movie.app.model.dto.favorite.response.FavoriteResponse;
import com.movie.app.model.dto.favorite.response.FavoritesStatsResponse;
import com.movie.app.security.UserPrincipal;
import com.movie.app.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;

    //Agregar pelicula a favoritos
    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                     @Valid @RequestBody AddFavoriteRequest request) {
        log.info("POST /api/favorites - User: {}, Movie: {}",
                userPrincipal.getId(), request.getMovieId());

        FavoriteResponse favorite = favoriteService.addFavorite(userPrincipal.getId(), request);

        ApiResponse<FavoriteResponse> response = ApiResponse.<FavoriteResponse>builder()
                .success(true)
                .message("Película agregada a favoritos")
                .data(favorite)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //Obtener favoritos por paginación
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FavoriteResponse>>> getUserFavorites(@AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/favorites - User: {}, Page: {}, Size: {}",
                userPrincipal.getId(), page, size);

        PageResponse<FavoriteResponse> favorites = favoriteService.getUserFavorites(
                userPrincipal.getId(), page, size
        );
        ApiResponse<PageResponse<FavoriteResponse>> response =
                ApiResponse.<PageResponse<FavoriteResponse>>builder()
                        .success(true)
                        .data(favorites)
                        .build();

        return ResponseEntity.ok(response);
    }

    //Verificar si es una pelicula
    @GetMapping("/check/{movieId}")
    public ResponseEntity<ApiResponse<Boolean>> isFavorite(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer movieId
    ) {
        log.info("GET /api/favorites/check/{} - User: {}", movieId, userPrincipal.getId());

        boolean isFavorite = favoriteService.isFavorite(userPrincipal.getId(), movieId);

        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(true)
                .data(isFavorite)
                .build();

        return ResponseEntity.ok(response);
    }

    //Obtener id solo de pelis favoritas
    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<List<Integer>>> getFavoriteMovieIds(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/favorites/ids - User: {}", userPrincipal.getId());

        List<Integer> movieIds = favoriteService.getFavoriteMovieIds(userPrincipal.getId());
        ApiResponse<List<Integer>> response = ApiResponse.<List<Integer>>builder()
                .success(true)
                .data(movieIds)
                .build();

        return ResponseEntity.ok(response);
    }

    //Obtener estadisticas de favoritos
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<FavoritesStatsResponse>> getFavoritesStats(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/favorites/stats - User: {}", userPrincipal.getId());

        FavoritesStatsResponse stats = favoriteService.getFavoritesStats(userPrincipal.getId());
        ApiResponse<FavoritesStatsResponse> response = ApiResponse.<FavoritesStatsResponse>builder()
                .success(true)
                .data(stats)
                .build();

        return ResponseEntity.ok(response);
    }

    //Eliminar pelicula de favoritos
    @DeleteMapping("/{movieId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer movieId) {
        log.info("DELETE /api/favorites/{} - User: {}", movieId, userPrincipal.getId());

        favoriteService.removeFavorite(userPrincipal.getId(), movieId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Película eliminada de favoritos")
                .build();

        return ResponseEntity.ok(response);
    }
}