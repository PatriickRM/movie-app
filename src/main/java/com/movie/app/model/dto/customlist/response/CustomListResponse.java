package com.movie.app.model.dto.customlist.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomListResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isPublic;
    private Integer movieCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}