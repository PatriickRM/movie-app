package com.movie.app.model.dto.ai.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieRecommendation {
    private Integer movieId;
    private String title;
    private String reason;
}
