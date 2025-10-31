package com.movie.app.model.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TMDbMovieDetails {

    private Integer id;
    private String title;
    @JsonProperty("original_title")
    private String originalTitle;
    private String overview;
    @JsonProperty("poster_path")
    private String posterPath;
    @JsonProperty("backdrop_path")
    private String backdropPath;
    @JsonProperty("release_date")
    private String releaseDate;
    @JsonProperty("vote_average")
    private Double voteAverage;
    @JsonProperty("vote_count")
    private Integer voteCount;
    private Integer runtime;
    private String status;
    private String tagline;
    private List<Genre> genres;
    @JsonProperty("production_companies")
    private List<ProductionCompany> productionCompanies;
    private Long budget;
    private Long revenue;
    @JsonProperty("imdb_id")
    private String imdbId;
    private String homepage;
}