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
import ru.yandex.practicum.filmorate.util.EventType;
import ru.yandex.practicum.filmorate.util.OperationType;

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
    private final EventService eventService;

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
        eventService.createEvent(userId, EventType.LIKE.name(), OperationType.ADD.name(), filmId);
    }

    public void removeLike(long filmId, long userId) {
        if (!filmStorage.existsFilm(filmId)) {
            throw new NotFoundException("Фильм не найден: " + filmId);
        }
        if (!userStorage.existsUser(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        filmStorage.deleteLike(filmId, userId);
        eventService.createEvent(userId, EventType.LIKE.name(), OperationType.REMOVE.name(), filmId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getPopularFilms(count)
                .stream()
                .collect(Collectors.toList());
    }

    public Collection<Film> getRecommendations(int userId) {
        return filmStorage.getRecommendedFilms(userId);
    }
  
    public Collection<Film> getFilmsByDirector(int directorId, String sortBy) {
        if (sortBy.equals("year")) {
            return filmStorage.getFilmsByDirectorSortedByYear(directorId);
        } else if (sortBy.equals("likes")) {
            return filmStorage.getFilmsByDirectorSortedByLikes(directorId);
        } else {
            throw new IllegalArgumentException("sortBy должен быть 'year' или 'likes'");
        }
    }

    public Collection<Film> getCommonFilms(long userId, long friendId) {
        if (!userStorage.existsUser(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        if (!userStorage.existsUser(friendId)) {
            throw new NotFoundException("Пользователь не найден: " + friendId);
        }
        return filmStorage.getCommonFilms(userId, friendId);
    }
}
