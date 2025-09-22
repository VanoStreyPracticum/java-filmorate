package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
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

    public Collection<Film> getAll() {
        return filmStorage.getAllFilms();
    }

    public Film getById(long id) {
        return filmStorage.getFilm(id).orElseThrow(() -> new NotFoundException("Фильм не найден: " + id));
    }

    public void addLike(long filmId, long userId) {
        Film film = getById(filmId);
        if (!userStorage.existsUser(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        film.getLikes().add(userId);
    }

    public void removeLike(long filmId, long userId) {
        Film film = getById(filmId);
        if (!userStorage.existsUser(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        film.getLikes().remove(userId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }
}