package com.movie.app.model.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {
    @NotBlank(message = "El refresh token es requerido")
    private String refreshToken;
}