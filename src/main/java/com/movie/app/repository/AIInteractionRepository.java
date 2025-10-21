package com.movie.app.repository;

import com.movie.app.model.entity.AIInteraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AIInteractionRepository extends JpaRepository<AIInteraction, Long> {
    //Buscar interacciones de un usuario con paginación
    Page<AIInteraction> findByUserId(Long userId, Pageable pageable);
    long countByUserId(Long userId);
    //Buscar últimas interacciones de un usuario
    @Query("SELECT ai FROM AIInteraction ai WHERE ai.user.id = :userId ORDER BY ai.createdAt DESC")
    List<AIInteraction> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
    //Contar interacciones de hoy de un usuario
    @Query("SELECT COUNT(ai) FROM AIInteraction ai WHERE ai.user.id = :userId AND ai.createdAt >= :today")
    long countTodayByUserId(@Param("userId") Long userId, @Param("today") LocalDateTime today);
}
