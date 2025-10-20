package com.movie.app.model.dto.rating.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddRatingRequest {
    @NotNull(message = "El ID de la película es requerido")
    @Positive(message = "El ID de la película debe ser positivo")
    private Integer movieId;
    @NotNull(message = "La calificación es requerida")
    @DecimalMin(value = "1.0", message = "La calificación mínima es 1.0")
    @DecimalMax(value = "5.0", message = "La calificación máxima es 5.0")
    private BigDecimal rating;
    @Size(max = 5000, message = "La reseña no puede exceder 5000 caracteres")
    private String review;
}