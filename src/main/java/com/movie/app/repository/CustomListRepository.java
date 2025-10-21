package com.movie.app.repository;

import com.movie.app.model.entity.CustomList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomListRepository extends JpaRepository<CustomList, Long> {
    Page<CustomList> findByUserId(Long userId, Pageable pageable);
    List<CustomList> findByUserId(Long userId);
    Page<CustomList> findByIsPublicTrue(Pageable pageable);
    long countByUserId(Long userId);
    boolean existsByIdAndUserId(Long listId, Long userId);
    Optional<CustomList> findByIdAndUserId(Long listId, Long userId);
}
