package com.movie.app.model.dto.user.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String fullName;
}