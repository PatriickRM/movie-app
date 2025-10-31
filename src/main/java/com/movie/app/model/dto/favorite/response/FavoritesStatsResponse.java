package com.movie.app.model.dto.favorite.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritesStatsResponse {
    private Integer totalFavorites;
    private Integer maxFavorites;
    private Boolean canAddMore;
    private Boolean isPremium;
}