package com.movie.app.model.dto.rating.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingResponse {
    private Long id;
    private Integer movieId;
    private BigDecimal rating;
    private String review;
    private LocalDateTime watchedAt;
    private LocalDateTime updatedAt;
}