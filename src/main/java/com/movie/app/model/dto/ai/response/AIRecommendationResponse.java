package com.movie.app.model.dto.ai.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIRecommendationResponse {
    private List<MovieRecommendation> recommendations;
    private String explanation;
    private Integer requestsRemainingToday;
}
