package com.movie.app.model.dto.auth.response;

import com.movie.app.model.entity.UserPlan;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private UserPlan plan;
    private Integer aiRequestsToday;
    private Integer maxFavorites;
    private LocalDateTime premiumUntil;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
}