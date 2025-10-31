package com.movie.app.model.dto.customlist.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddMovieToListRequest {
    @NotNull(message = "El ID de la película es requerido")
    @Positive(message = "El ID de la película debe ser positivo")
    private Integer movieId;
    private String movieTitle;
    private String moviePoster;
}