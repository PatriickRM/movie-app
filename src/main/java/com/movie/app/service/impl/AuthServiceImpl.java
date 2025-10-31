package com.movie.app.service.impl;

import com.movie.app.exception.BadRequestException;
import com.movie.app.exception.ResourceNotFoundException;
import com.movie.app.model.dto.auth.request.LoginRequest;
import com.movie.app.model.dto.auth.request.RefreshTokenRequest;
import com.movie.app.model.dto.auth.request.RegisterRequest;
import com.movie.app.model.dto.auth.response.AuthResponse;
import com.movie.app.model.dto.auth.response.UserResponse;
import com.movie.app.model.entity.RefreshToken;
import com.movie.app.model.entity.User;
import com.movie.app.model.entity.UserPlan;
import com.movie.app.repository.RefreshTokenRepository;
import com.movie.app.repository.UserRepository;
import com.movie.app.security.JwtTokenProvider;
import com.movie.app.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;
    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    //Registrar nuevo usuario
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registrando nuevo usuario: {}", request.getEmail());

        // Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }

        // Crear usuario
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .plan(UserPlan.FREE)
                .maxFavorites(3)
                .aiRequestsToday(0)
                .isActive(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        // Generar tokens
        String accessToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());
        String refreshToken = generateAndSaveRefreshToken(user);

        log.info("Usuario registrado exitosamente: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .user(mapToUserResponse(user))
                .build();
    }

    //Inicio sesión
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Intentando login para: {}", request.getEmail());

        // Buscar usuario
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Credenciales inválidas"));

        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Credenciales inválidas");
        }

        // Verificar que el usuario esté activo
        if (!user.getIsActive()) {
            throw new BadRequestException("La cuenta está desactivada");
        }
        // Autenticar
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getId().toString(),
                        request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generar tokens
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = generateAndSaveRefreshToken(user);

        log.info("Login exitoso para: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .user(mapToUserResponse(user))
                .build();
    }


    //Refrescar access token
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refrescando token");

        // Buscar refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Refresh token inválido"));

        // Validar token
        if (refreshToken.getIsRevoked()) {
            throw new BadRequestException("Refresh token revocado");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token expirado");
        }

        // Generar nuevo access token
        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        log.info("Token refrescado para usuario: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .user(mapToUserResponse(user))
                .build();
    }

    //Cerrar sesión (revocar refresh token)
    @Transactional
    public void logout(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token no encontrado"));

        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.info("Logout exitoso para usuario: {}", refreshToken.getUser().getEmail());
    }

    //Cerrar sesión
    @Transactional
    public void logoutAll(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Logout de todos los dispositivos para usuario ID: {}", userId);
    }

    //METODOS PRIVADOS

    //Guardar y generar refreshtoken
    private String generateAndSaveRefreshToken(User user) {
        String token = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    //MAPEAR USER A USERRESPONSE
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .plan(user.getPlan())
                .aiRequestsToday(user.getAiRequestsToday())
                .maxFavorites(user.getMaxFavorites())
                .premiumUntil(user.getPremiumUntil())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}