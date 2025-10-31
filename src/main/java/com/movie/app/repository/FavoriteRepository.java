package com.movie.app.repository;

import com.movie.app.model.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long>{
    Page<Favorite> findByUserId(Long userId, Pageable pageable);
    List<Favorite> findByUserId(Long userId);
    long countByUserId(Long userId);
    boolean existsByUserIdAndMovieId(Long userId, Integer movieId);
    Optional<Favorite> findByUserIdAndMovieId(Long userId, Integer movieId);
    void deleteByUserIdAndMovieId(Long userId, Integer movieId);

    @Query("SELECT f.movieId FROM Favorite f WHERE f.user.id = :userId")
    List<Integer> findMovieIdsByUserId(@Param("userId") Long userId);

}
