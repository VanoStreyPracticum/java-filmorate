package ru.yandex.practicum.filmorate.mapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.film.FilmDTO;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FilmMapper {

    @Qualifier("mpaDbStorage")
    private final MpaDbStorage mpaDbStorage;
    @Qualifier("genreDbStorage")
    private final GenreDbStorage genreDbStorage;
    private final MpaMapper mpaMapper;
    private final GenreMapper genreMapper;

    public FilmMapper(MpaDbStorage mpaDbStorage, GenreDbStorage genreDbStorage, MpaMapper mpaMapper, GenreMapper genreMapper) {
        this.mpaDbStorage = mpaDbStorage;
        this.genreDbStorage = genreDbStorage;
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
                film.getLikes()
        );
    }

    public Film fromNewRequest(NewFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        film.setMpa(request.getMpaId() != null ? mpaDbStorage.findById(request.getMpaId()) : null);

        if (request.getGenreIds() != null) {
            film.setGenres(request.getGenreIds().stream()
                    .map(genreDbStorage::findById)
                    .collect(Collectors.toSet()));
        } else {
            film.setGenres(new HashSet<>());
        }

        film.setLikes(new HashSet<>());
        return film;
    }

    // **ВАЖНО:** обновление существующего фильма
    public Film fromUpdateRequest(UpdateFilmRequest request, Film existingFilm) {
        existingFilm.setName(request.getName() != null ? request.getName() : existingFilm.getName());
        existingFilm.setDescription(request.getDescription() != null ? request.getDescription() : existingFilm.getDescription());
        existingFilm.setReleaseDate(request.getReleaseDate() != null ? request.getReleaseDate() : existingFilm.getReleaseDate());
        existingFilm.setDuration(request.getDuration() != null ? request.getDuration() : existingFilm.getDuration());
        existingFilm.setMpa(request.getMpaId() != null ? mpaDbStorage.findById(request.getMpaId()) : existingFilm.getMpa());

        if (request.getGenreIds() != null) {
            existingFilm.setGenres(request.getGenreIds().stream()
                    .map(genreDbStorage::findById)
                    .collect(Collectors.toSet()));
        }

        return existingFilm;
    }
}
