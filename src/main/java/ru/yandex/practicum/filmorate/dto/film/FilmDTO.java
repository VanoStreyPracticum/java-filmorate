package ru.yandex.practicum.filmorate.dto.film;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.yandex.practicum.filmorate.dto.genre.GenresDTO;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class FilmDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;

    private MpaDTO mpa;

    private List<GenresDTO> genres;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<Long> likes;
}