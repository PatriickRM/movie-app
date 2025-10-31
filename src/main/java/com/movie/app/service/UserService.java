package com.movie.app.service;

import com.movie.app.model.dto.auth.response.UserResponse;
import com.movie.app.model.dto.user.request.ChangePasswordRequest;
import com.movie.app.model.dto.user.request.UpdateProfileRequest;
import com.movie.app.model.dto.user.response.UserStatsResponse;

public interface UserService {
    UserResponse getCurrentUser(Long userId);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    UserStatsResponse getUserStats(Long userId);
    boolean isPremiumUser(Long userId);
}
