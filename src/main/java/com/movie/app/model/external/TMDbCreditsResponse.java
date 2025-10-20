package com.movie.app.model.external;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TMDbCreditsResponse {
    private Integer id;
    private List<Cast> cast;
    private List<Crew> crew;
}