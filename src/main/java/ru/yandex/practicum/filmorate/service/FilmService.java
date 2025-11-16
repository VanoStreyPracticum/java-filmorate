package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final ValidationService validationService;

    public Film create(Film film) {
        validationService.validateNewFilm(film);
        return filmStorage.addFilm(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("ID обязателен для обновления фильма");
        }
        validationService.validateNewFilm(film);
        return filmStorage.updateFilm(film);
    }

    public void delete(long id) {
        if (!filmStorage.existsFilm(id)) {
            throw new NotFoundException("Фильм не найден: " + id);
        }
        filmStorage.deleteFilm(id);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAllFilms();
    }

    public Film getById(long id) {
        return filmStorage.getFilm(id)
                .orElseThrow(() -> new NotFoundException("Фильм не найден: " + id));
    }

    public void addLike(long filmId, long userId) {
        if (!filmStorage.existsFilm(filmId)) {
            throw new NotFoundException("Фильм не найден: " + filmId);
        }
        if (!userStorage.existsUser(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        if (!filmStorage.existsFilm(filmId)) {
            throw new NotFoundException("Фильм не найден: " + filmId);
        }
        if (!userStorage.existsUser(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        filmStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> getPopular(Integer count, Integer genreId, Integer year) {
        int limit = (count == null || count <= 0) ? 10 : count;

        if (genreId == null && year == null) {
            return filmStorage.getPopularFilms(limit);
        }

        return filmStorage.getPopularFilms(limit, genreId, year);
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        if (sortBy.equals("year")) {
            return (List<Film>) filmStorage.getFilmsByDirectorSortedByYear(directorId);
        } else if (sortBy.equals("likes")) {
            return (List<Film>) filmStorage.getFilmsByDirectorSortedByLikes(directorId);
        } else {
            throw new IllegalArgumentException("sortBy должен быть 'year' или 'likes'");
        }
    }

}
