package com.movie.app.repository;

import com.movie.app.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query("SELECT u FROM User u WHERE u.plan = 'PREMIUM' AND u.premiumUntil > :now")
    List<User> findActivePremiumUsers(@Param("now") LocalDateTime now);

     //Resetear contador de AI requests para free users
    @Modifying
    @Query("UPDATE User u SET u.aiRequestsToday = 0, u.aiLastReset = :now " +
            "WHERE u.plan = 'FREE' AND (u.aiLastReset IS NULL OR u.aiLastReset < :today)")
    int resetDailyAIRequests(@Param("now") LocalDateTime now, @Param("today") LocalDateTime today);
}
