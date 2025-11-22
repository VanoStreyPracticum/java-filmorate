package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.film.FilmDTO;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.util.EventType;
import ru.yandex.practicum.filmorate.util.OperationType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

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
    private final FilmMapper filmMapper;

    public FilmDTO create(NewFilmRequest newFilmRequest) {
        log.info("Добавлен фильм: {}", newFilmRequest);
        Film film = filmMapper.fromNewRequest(newFilmRequest);
        log.info("Фильм после маппинга {}", film);
        validationService.validateNewFilm(film);
        film = filmStorage.addFilm(film);
        log.info("Фильм создан в БД {}", film);
        return filmMapper.toDTO(film);
    }

    public FilmDTO update(UpdateFilmRequest updateFilmRequest) {
        Optional<Film> optionalFilm = filmStorage.getFilm(updateFilmRequest.getId());
        if (optionalFilm.isEmpty()) {
            log.error("Не удалось найти в базе фильм по ID {}", updateFilmRequest.getId());
            throw new NotFoundException("ID обязателен для обновления фильма");
        }
        Film film = filmMapper.fromUpdateRequest(updateFilmRequest, optionalFilm.get());
        validationService.validateNewFilm(film);
        log.info("Обновлён фильм: id={}, name='{}'", film.getId(), film.getName());
        film = filmStorage.updateFilm(film);
        return filmMapper.toDTO(film);
    }

    public void delete(long id) {
        filmStorage.deleteFilm(id);
        log.info("Удалён фильм: id={}", id);
    }

    public Collection<FilmDTO> getAll() {
        Collection<Film> filmList = filmStorage.getAllFilms();
        log.info("Получен список фильмов {}", filmList);
        return filmMapper.toCollectionDto(filmList);
    }

    public FilmDTO getById(long id) {
        log.info("Получение из базы фильма по ID {}", id);
        Optional<Film> optionalFilm = filmStorage.getFilm(id);
        if (optionalFilm.isEmpty()) {
            log.error("Не удалось найти в базе фильм c ID {}", id);
            throw new NotFoundException("ID обязателен для обновления фильма");
        }
        Film film = optionalFilm.get();
        log.info("Получен фильм {}", film);
        return filmMapper.toDTO(film);
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

    public Collection<FilmDTO> getPopularFilms(Integer count, Integer genreId, Integer year) {
        log.info("Получение фильмов в количестве {} по жанру {} и году {}", count, genreId, year);
        int limit = (count == null || count <= 0) ? 10 : count;
        if (genreId == null && year == null) {
            Collection<Film> films = filmStorage.getPopularFilms(limit);
            log.info("Получен список популярных фильмов {}", films);
            return filmMapper.toCollectionDto(films);
        }
        Collection<Film> films = filmStorage.getPopularFilms(limit, genreId, year);
        log.info("Получен список популярных фильмов {}", films);
        return filmMapper.toCollectionDto(films);
    }

    public Collection<FilmDTO> getRecommendations(int userId) {
        Collection<Film> films = filmStorage.getRecommendedFilms(userId);
        log.info("Получен список рекомендованых фильмов {}", films);
        return filmMapper.toCollectionDto(films);
    }

    public Collection<FilmDTO> getFilmsByDirector(int directorId, String sortBy) {
        Collection<Film> result;
        if (sortBy.equals("year")) {
            result = filmStorage.getFilmsByDirectorSortedByYear(directorId);
            if (result.isEmpty()) {
                log.error("Не найдено фильмов режисера с id: {}", directorId);
                throw new NotFoundException("Не найдено фильмов режисера с id: " + directorId);
            }
        } else if (sortBy.equals("likes")) {
            result = filmStorage.getFilmsByDirectorSortedByLikes(directorId);
            if (result.isEmpty()) {
                log.error("Не найдено фильмов режисера по id: {}", directorId);
                throw new NotFoundException("Не найдено фильмов режисера с id: " + directorId);
            }
        } else {
            log.error("Неверное значение сортировки {}", sortBy);
            throw new IllegalArgumentException("sortBy должен быть 'year' или 'likes'");
        }
        return filmMapper.toCollectionDto(result);
    }

    public Collection<FilmDTO> getCommonFilms(long userId, long friendId) {
        if (!userStorage.existsUser(userId)) {
            log.error("Пользователь не найден по ID {}", userId);
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        if (!userStorage.existsUser(friendId)) {
            log.error("Пользователь не найден по ID {}", userId);
            throw new NotFoundException("Пользователь не найден: " + friendId);
        }
        Collection<Film> films = filmStorage.getCommonFilms(userId, friendId);
        return filmMapper.toCollectionDto(films);
    }

    public Collection<FilmDTO> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            log.error("Параметр query не должен быть пустым {}", query);
            throw new ValidationException("Параметр query не должен быть пустым");
        }

        Collection<String> fields = Arrays.stream(by.split(","))
                .map(String::trim).toList();

        if (fields.isEmpty()) {
            log.error("Не указано поле поиска (by=title,director) {}", by);
            throw new ValidationException("Не указано поле поиска (by=title,director)");
        }

        Collection<Film> films = filmStorage.searchFilms(query.toLowerCase(), fields);
        return filmMapper.toCollectionDto(films);
    }
}
