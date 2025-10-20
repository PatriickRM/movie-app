package com.movie.app.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "list_movies", uniqueConstraints = @UniqueConstraint(columnNames = {"list_id", "movie_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListMovie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    private CustomList customList;

    @Column(name = "movie_id", nullable = false)
    private Integer movieId;

    @Column(name = "movie_title", length = 500)
    private String movieTitle;

    @Column(name = "movie_poster", length = 500)
    private String moviePoster;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;
}
