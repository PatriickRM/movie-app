package com.movie.app.model.external;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TMDbGenreListResponse {
    private List<Genre> genres;
}