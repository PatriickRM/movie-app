package com.movie.app.model.dto.customlist.response;

import lombok.* ;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListMovieResponse {
    private Integer movieId;
    private String movieTitle;
    private String moviePoster;
    private LocalDateTime addedAt;
}