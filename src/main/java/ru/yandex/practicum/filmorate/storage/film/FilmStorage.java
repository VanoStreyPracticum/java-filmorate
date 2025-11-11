package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(long id);

    Optional<Film> getFilm(long id);

    boolean existsFilm(long id);

    Collection<Film> getAllFilms();

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Collection<Film> getPopularFilms(int count);

    Collection<Film> getFilmsByDirectorSortedByYear(int directorId);

    Collection<Film> getFilmsByDirectorSortedByLikes(int directorId);
}
