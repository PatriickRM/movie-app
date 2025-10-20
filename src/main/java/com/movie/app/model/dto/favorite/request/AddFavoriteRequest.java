package com.movie.app.model.dto.favorite.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddFavoriteRequest {
    @NotNull(message = "El ID de la película es requerido")
    @Positive(message = "El ID de la película debe ser positivo")
    private Integer movieId;
    private String movieTitle;
    private String moviePoster;
    private String movieOverview;
    private String releaseDate;
    private Double voteAverage;
}