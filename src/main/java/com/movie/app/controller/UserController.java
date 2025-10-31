package com.movie.app.controller;

import com.movie.app.model.dto.auth.response.UserResponse;
import com.movie.app.model.dto.common.ApiResponse;
import com.movie.app.model.dto.user.request.ChangePasswordRequest;
import com.movie.app.model.dto.user.request.UpdateProfileRequest;
import com.movie.app.model.dto.user.response.UserStatsResponse;
import com.movie.app.security.UserPrincipal;
import com.movie.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    //Obtener perfil de usuario actual
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/user/me - User ID: {}", userPrincipal.getId());

        UserResponse user = userService.getCurrentUser(userPrincipal.getId());
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .data(user)
                .build();

        return ResponseEntity.ok(response);
    }

    //Actualizar perfil de usuario
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@AuthenticationPrincipal UserPrincipal userPrincipal, @Valid @RequestBody UpdateProfileRequest request) {
        log.info("PUT /api/user/profile - User ID: {}", userPrincipal.getId());

        UserResponse user = userService.updateProfile(userPrincipal.getId(), request);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Perfil actualizado exitosamente")
                .data(user)
                .build();

        return ResponseEntity.ok(response);
    }

    //Cambiar contraseña
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal UserPrincipal userPrincipal, @Valid @RequestBody ChangePasswordRequest request) {
        log.info("PUT /api/user/password - User ID: {}", userPrincipal.getId());

        userService.changePassword(userPrincipal.getId(), request);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Contraseña cambiada exitosamente")
                .build();

        return ResponseEntity.ok(response);
    }

    //Estadisticas del usuario
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/user/stats - User ID: {}", userPrincipal.getId());

        UserStatsResponse stats = userService.getUserStats(userPrincipal.getId());

        ApiResponse<UserStatsResponse> response = ApiResponse.<UserStatsResponse>builder()
                .success(true)
                .data(stats)
                .build();

        return ResponseEntity.ok(response);
    }
}
