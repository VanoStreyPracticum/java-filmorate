package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.film.FilmDTO;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FilmMapper {
    private final MpaMapper mpaMapper;
    private final GenreMapper genreMapper;

    public FilmMapper(MpaMapper mpaMapper, GenreMapper genreMapper) {
        this.mpaMapper = mpaMapper;
        this.genreMapper = genreMapper;
    }

    public FilmDTO toDTO(Film film) {
        return new FilmDTO(
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? mpaMapper.toDTO(film.getMpa()) : null,
                genreMapper.toDTOSet(film.getGenres()),
                film.getLikes(),
                film.getDirectors()
        );
    }

    public Film fromNewRequest(NewFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        film.setMpa(request.getMpa() != null ? new Mpa(request.getMpa().getId(), null) : null);
        if (request.getGenres() != null) {
            film.setGenres(request.getGenres().stream()
                    .map(genre -> new Genre(genre.getId(), null))
                    .collect(Collectors.toSet()));
        } else {
            film.setGenres(new HashSet<>());
        }
        if (request.getDirectors() != null) {
            film.setDirectors(request.getDirectors().stream()
                    .map(director -> new Director(director.getId(), null))
                    .collect(Collectors.toSet()));
        } else {
            film.setDirectors(new HashSet<>());
        }

        film.setLikes(new HashSet<>());
        return film;
    }

    public Film fromUpdateRequest(UpdateFilmRequest request, Film film) {
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (request.hasMpa()) {
            film.setMpa(new Mpa(request.getMpa().getId(), null));
        }
        if (request.hasGenres()) {
            Set<Genre> genres = request.getGenres().stream()
                    .map(genre -> new Genre(genre.getId(), null))
                    .collect(Collectors.toSet());
            film.setGenres(genres);
        }
        if (request.getDirectors() != null) {
            film.setDirectors(request.getDirectors().stream()
                    .map(director -> new Director(director.getId(), null))
                    .collect(Collectors.toSet()));
        } else {
            film.setDirectors(new HashSet<>());
        }
        return film;
    }

    public Collection<FilmDTO> toCollectionDto(Collection<Film> films) {
        return films.stream().map(this::toDTO).toList();
    }
}
