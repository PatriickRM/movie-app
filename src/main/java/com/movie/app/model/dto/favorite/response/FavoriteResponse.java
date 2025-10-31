package com.movie.app.model.dto.favorite.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteResponse {
    private Long id;
    private Integer movieId;
    private String movieTitle;
    private String moviePoster;
    private String movieOverview;
    private String releaseDate;
    private Double voteAverage;
    private LocalDateTime addedAt;
}
