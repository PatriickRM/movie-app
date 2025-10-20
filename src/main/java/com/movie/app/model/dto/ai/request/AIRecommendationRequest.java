package com.movie.app.model.dto.ai.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRecommendationRequest {

    @NotBlank(message = "El prompt es requerido")
    @Size(max = 1000, message = "El prompt no puede exceder 1000 caracteres")
    private String prompt;

    @Builder.Default
    private Boolean includeUserHistory = true;

    @Builder.Default
    private Integer maxRecommendations = 5;
}