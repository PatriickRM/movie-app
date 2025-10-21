package com.movie.app.service;

import com.movie.app.model.dto.common.PageResponse;
import com.movie.app.model.dto.rating.request.AddRatingRequest;
import com.movie.app.model.dto.rating.request.UpdateRatingRequest;
import com.movie.app.model.dto.rating.response.RatingResponse;

import java.util.List;

public interface RatingService {
    RatingResponse addOrUpdateRating(Long userId, AddRatingRequest request);
    RatingResponse updateRating(Long userId, Integer movieId, UpdateRatingRequest request);
    void deleteRating(Long userId, Integer movieId);
    PageResponse<RatingResponse> getUserRatings(Long userId, int page, int size);
    RatingResponse getRatingByMovie(Long userId, Integer movieId);
    boolean hasRated(Long userId, Integer movieId);
    Double getUserAverageRating(Long userId);
    List<RatingResponse> getTopRatedMovies(Long userId, int limit);
    List<Integer> getRatedMovieIds(Long userId);
}
