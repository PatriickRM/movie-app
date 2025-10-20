package com.movie.app.model.dto.customlist.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomListDetailResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isPublic;
    private List<ListMovieResponse> movies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}