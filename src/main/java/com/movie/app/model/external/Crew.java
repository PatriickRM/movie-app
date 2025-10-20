package com.movie.app.model.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Crew {
    private Integer id;
    private String name;
    private String job;
    private String department;
    @JsonProperty("profile_path")
    private String profilePath;
}