package com.movie.app.service;

import com.movie.app.model.dto.common.PageResponse;
import com.movie.app.model.dto.favorite.request.AddFavoriteRequest;
import com.movie.app.model.dto.favorite.response.FavoriteResponse;
import com.movie.app.model.dto.favorite.response.FavoritesStatsResponse;

import java.util.List;

public interface FavoriteService {
    FavoriteResponse addFavorite(Long userId, AddFavoriteRequest request);
    void removeFavorite(Long userId, Integer movieId);
    PageResponse<FavoriteResponse> getUserFavorites(Long userId, int page, int size);
    boolean isFavorite(Long userId, Integer movieId);
    List<Integer> getFavoriteMovieIds(Long userId);
    FavoritesStatsResponse getFavoritesStats(Long userId);
}