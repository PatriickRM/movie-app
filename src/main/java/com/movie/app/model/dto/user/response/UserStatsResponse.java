package com.movie.app.model.dto.user.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsResponse {
    private Integer totalFavorites;
    private Integer totalRatings;
    private Integer totalLists;
    private Double averageRating;
    private Integer totalAIRequests;
}