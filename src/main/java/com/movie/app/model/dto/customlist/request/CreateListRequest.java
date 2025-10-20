package com.movie.app.model.dto.customlist.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateListRequest {
    @NotBlank(message = "El nombre de la lista es requerido")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String name;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;

    @Builder.Default
    private Boolean isPublic = false;
}