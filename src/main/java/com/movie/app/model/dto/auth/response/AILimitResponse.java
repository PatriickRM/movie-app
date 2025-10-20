package com.movie.app.model.dto.auth.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AILimitResponse {
    private Boolean canRequest;
    private Integer requestsRemainingToday;
    private Boolean isPremium;
    private String message;
}