package com.movie.app.model.external;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TMDbVideoResponse {
    private Integer id;
    private List<TMDbVideo> results;
}