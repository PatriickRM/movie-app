package com.movie.app.repository;

import com.movie.app.model.entity.ListMovie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ListMovieRepository extends JpaRepository<ListMovie, Long> {
    List<ListMovie> findByCustomListId(Long listId);
    long countByCustomListId(Long listId);
    boolean existsByCustomListIdAndMovieId(Long listId, Integer movieId);

    //Buscar película específica en una lista
    Optional<ListMovie> findByCustomListIdAndMovieId(Long listId, Integer movieId);
    //Eliminar película de una lista
    void deleteByCustomListIdAndMovieId(Long listId, Integer movieId);
    void deleteByCustomListId(Long listId);
}
