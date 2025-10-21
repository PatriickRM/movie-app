package com.movie.app.service.impl;

import com.movie.app.exception.BadRequestException;
import com.movie.app.exception.ResourceNotFoundException;
import com.movie.app.model.dto.*;
import com.movie.app.model.dto.common.PageResponse;
import com.movie.app.model.dto.favorite.request.AddFavoriteRequest;
import com.movie.app.model.dto.favorite.response.FavoriteResponse;
import com.movie.app.model.dto.favorite.response.FavoritesStatsResponse;
import com.movie.app.model.entity.Favorite;
import com.movie.app.model.entity.User;
import com.movie.app.model.entity.UserPlan;
import com.movie.app.repository.FavoriteRepository;
import com.movie.app.repository.UserRepository;
import com.movie.app.service.FavoriteService;
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
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    //Agregar pelicula a favoritos
    @Transactional
    public FavoriteResponse addFavorite(Long userId, AddFavoriteRequest request) {
        log.info("Agregando película {} a favoritos del usuario {}", request.getMovieId(), userId);

        User user = findUserById(userId);

        // Verificar si ya existe
        if (favoriteRepository.existsByUserIdAndMovieId(userId, request.getMovieId())) {
            throw new BadRequestException("La película ya está en favoritos");
        }

        // Verificar límite
        if (user.getPlan() == UserPlan.FREE) {
            long currentCount = favoriteRepository.countByUserId(userId);
            if (currentCount >= user.getMaxFavorites()) {
                throw new BadRequestException(
                        "Has alcanzado el límite de " + user.getMaxFavorites() +
                                " favoritos. Actualiza a Premium para favoritos ilimitados"
                );
            }
        }

        // Crear favorito
        Favorite favorite = Favorite.builder()
                .user(user)
                .movieId(request.getMovieId())
                .movieTitle(request.getMovieTitle())
                .moviePoster(request.getMoviePoster())
                .movieOverview(request.getMovieOverview())
                .releaseDate(request.getReleaseDate())
                .voteAverage(request.getVoteAverage())
                .build();

        favorite = favoriteRepository.save(favorite);

        log.info("Película {} agregada a favoritos exitosamente", request.getMovieId());

        return mapToFavoriteResponse(favorite);
    }

    //Eliminar pelicula de favoritos
    @Transactional
    public void removeFavorite(Long userId, Integer movieId) {
        log.info("Eliminando película {} de favoritos del usuario {}", movieId, userId);

        if (!favoriteRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw new ResourceNotFoundException("La película no está en favoritos");
        }

        favoriteRepository.deleteByUserIdAndMovieId(userId, movieId);

        log.info("Película {} eliminada de favoritos exitosamente", movieId);
    }

    //Obtener favoritos del usuario con paginación
    @Transactional(readOnly = true)
    public PageResponse<FavoriteResponse> getUserFavorites(Long userId, int page, int size) {
        log.info("Obteniendo favoritos del usuario {} - Página: {}, Tamaño: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("addedAt").descending());
        Page<Favorite> favoritesPage = favoriteRepository.findByUserId(userId, pageable);

        List<FavoriteResponse> favorites = favoritesPage.getContent().stream()
                .map(this::mapToFavoriteResponse)
                .collect(Collectors.toList());

        return PageResponse.<FavoriteResponse>builder()
                .content(favorites)
                .page(page)
                .size(size)
                .totalElements(favoritesPage.getTotalElements())
                .totalPages(favoritesPage.getTotalPages())
                .isLast(favoritesPage.isLast())
                .build();
    }

    //Verificar si una pelicula es favorita
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Integer movieId) {
        return favoriteRepository.existsByUserIdAndMovieId(userId, movieId);
    }

    //Obtener id de peliculas favoritas
    @Transactional(readOnly = true)
    public List<Integer> getFavoriteMovieIds(Long userId) {
        return favoriteRepository.findMovieIdsByUserId(userId);
    }

    //Obtener estadisticas de favoritos
    @Transactional(readOnly = true)
    public FavoritesStatsResponse getFavoritesStats(Long userId) {
        User user = findUserById(userId);
        long totalFavorites = favoriteRepository.countByUserId(userId);
        boolean isPremium = user.getPlan() == UserPlan.PREMIUM;

        int maxFavorites = isPremium ? -1 : user.getMaxFavorites();
        boolean canAddMore = isPremium || totalFavorites < maxFavorites;

        return FavoritesStatsResponse.builder()
                .totalFavorites((int) totalFavorites)
                .maxFavorites(maxFavorites)
                .canAddMore(canAddMore)
                .isPremium(isPremium)
                .build();
    }

    //Buscar usuario por id
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
    }

    //Mappear favorito a response
    private FavoriteResponse mapToFavoriteResponse(Favorite favorite) {
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .movieId(favorite.getMovieId())
                .movieTitle(favorite.getMovieTitle())
                .moviePoster(favorite.getMoviePoster())
                .movieOverview(favorite.getMovieOverview())
                .releaseDate(favorite.getReleaseDate())
                .voteAverage(favorite.getVoteAverage())
                .addedAt(favorite.getAddedAt())
                .build();
    }
}
