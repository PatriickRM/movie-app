package com.movie.app.model.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cast {
    private Integer id;
    private String name;
    private String character;
    @JsonProperty("profile_path")
    private String profilePath;
    private Integer order;
}