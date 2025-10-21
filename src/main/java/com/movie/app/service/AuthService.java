package com.movie.app.service;

import com.movie.app.model.dto.auth.request.LoginRequest;
import com.movie.app.model.dto.auth.request.RefreshTokenRequest;
import com.movie.app.model.dto.auth.request.RegisterRequest;
import com.movie.app.model.dto.auth.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String token);
    void logoutAll(Long userId);
}
