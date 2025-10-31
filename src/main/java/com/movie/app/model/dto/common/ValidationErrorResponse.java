package com.movie.app.model.dto.common;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationErrorResponse {
    private Integer status;
    private String error;
    private String message;
    private java.util.Map<String, String> errors;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}