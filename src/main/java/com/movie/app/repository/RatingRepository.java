package com.movie.app.repository;

import com.movie.app.model.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Page<Rating> findByUserId(Long userId, Pageable pageable);
    List<Rating> findByUserId(Long userId);
    Optional<Rating> findByUserIdAndMovieId(Long userId, Integer movieId);
    long countByUserId(Long userId);
    boolean existsByUserIdAndMovieId(Long userId, Integer movieId);
    //Obtener promedio de califacion por usuario
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.user.id = :userId")
    Double getAverageRatingByUserId(@Param("userId") Long userId);
    //Obtener top películas calificadas por un usuario
    @Query("SELECT r FROM Rating r WHERE r.user.id = :userId ORDER BY r.rating DESC")
    List<Rating> findTopRatedByUserId(@Param("userId") Long userId, Pageable pageable);

    void deleteByUserIdAndMovieId(Long userId, Integer movieId);
    //Obtener IDs de películas calificadas por un usuario
    @Query("SELECT r.movieId FROM Rating r WHERE r.user.id = :userId")
    List<Integer> findMovieIdsByUserId(@Param("userId") Long userId);
}
