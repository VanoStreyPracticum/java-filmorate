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

import java.util.Arrays;
import java.util.Collection;

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
        log.info("Добавлен фильм: id={}, name='{}'", film.getId(), film.getName());
        return filmStorage.addFilm(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("ID обязателен для обновления фильма");
        }
        validationService.validateNewFilm(film);
        log.info("Обновлён фильм: id={}, name='{}'", film.getId(), film.getName());
        return filmStorage.updateFilm(film);
    }

    public void delete(long id) {
        if (!filmStorage.existsFilm(id)) {
            throw new NotFoundException("Фильм не найден: " + id);
        }
        log.info("Удалён фильм: id={}", id);
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

    public Collection<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        int limit = (count == null || count <= 0) ? 10 : count;

        if (genreId == null && year == null) {
            return filmStorage.getPopularFilms(limit);
        }

        return filmStorage.getPopularFilms(limit, genreId, year);
    }

    public Collection<Film> getRecommendations(int userId) {
        return filmStorage.getRecommendedFilms(userId);
    }

    public Collection<Film> getFilmsByDirector(int directorId, String sortBy) {
        Collection<Film> result;
        if (sortBy.equals("year")) {
            result = filmStorage.getFilmsByDirectorSortedByYear(directorId);
            if (result.isEmpty()) {
                throw new NotFoundException("Не найдено фильмов режисера с id: " + directorId);
            }
        } else if (sortBy.equals("likes")) {
            result = filmStorage.getFilmsByDirectorSortedByLikes(directorId);
            if (result.isEmpty()) {
                throw new NotFoundException("Не найдено фильмов режисера с id: " + directorId);
            }
        } else {
            throw new IllegalArgumentException("sortBy должен быть 'year' или 'likes'");
        }
        return result;
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

    public Collection<Film> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Параметр query не должен быть пустым");
        }

        Collection<String> fields = Arrays.stream(by.split(","))
                .map(String::trim).toList();

        if (fields.isEmpty()) {
            throw new ValidationException("Не указано поле поиска (by=title,director)");
        }

        return filmStorage.searchFilms(query.toLowerCase(), fields);
    }
}
