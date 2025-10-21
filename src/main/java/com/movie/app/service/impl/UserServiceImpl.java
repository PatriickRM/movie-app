package com.movie.app.service.impl;

import com.movie.app.exception.BadRequestException;
import com.movie.app.exception.ResourceNotFoundException;
import com.movie.app.model.dto.auth.response.UserResponse;
import com.movie.app.model.dto.user.request.ChangePasswordRequest;
import com.movie.app.model.dto.user.request.UpdateProfileRequest;
import com.movie.app.model.dto.user.response.UserStatsResponse;
import com.movie.app.model.entity.User;
import com.movie.app.repository.*;
import com.movie.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final RatingRepository ratingRepository;
    private final CustomListRepository customListRepository;
    private final AIInteractionRepository aiInteractionRepository;
    private final PasswordEncoder passwordEncoder;

    //Obtener informacion de usuario actual
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = findUserById(userId);
        return mapToUserResponse(user);
    }

    //Actualizar perfil usuario
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Actualizando perfil del usuario ID: {}", userId);
        User user = findUserById(userId);
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        user = userRepository.save(user);
        log.info("Perfil actualizado para usuario: {}", user.getEmail());
        return mapToUserResponse(user);
    }

    //Cambiar contraseña
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Cambiando contraseña para usuario ID: {}", userId);

        User user = findUserById(userId);

        //Verificar contraseña actual
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }
        //Validar que la nueva contraseña sea diferente
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("La nueva contraseña debe ser diferente a la actual");
        }
        //Actualizar contraseña
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Contraseña cambiada exitosamente para usuario: {}", user.getEmail());
    }

    //Obtener estadísticas de usuario (solo premium)
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(Long userId) {
        log.info("Obteniendo estadísticas para usuario ID: {}", userId);

        User user = findUserById(userId);

        //Contar elementos
        long totalFavorites = favoriteRepository.countByUserId(userId);
        long totalRatings = ratingRepository.countByUserId(userId);
        long totalLists = customListRepository.countByUserId(userId);
        long totalAIRequests = aiInteractionRepository.countByUserId(userId);

        //Calcular promedio de ratings
        Double averageRating = ratingRepository.getAverageRatingByUserId(userId);
        if (averageRating == null) {
            averageRating = 0.0;
        }

        return UserStatsResponse.builder()
                .totalFavorites((int) totalFavorites)
                .totalRatings((int) totalRatings)
                .totalLists((int) totalLists)
                .totalAIRequests((int) totalAIRequests)
                .averageRating(averageRating)
                .build();
    }

   //Verificar si puede usar funciones premium
    @Transactional(readOnly = true)
    public boolean isPremiumUser(Long userId) {
        User user = findUserById(userId);
        return user.getPlan().name().equals("PREMIUM");
    }

    //buscar user por id
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
    }

    //Mapear user a response
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
