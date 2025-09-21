package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);
    Film updateFilm(Film film);
    Optional<Film> getFilm(long id);
    boolean existsFilm(long id);
    Collection<Film> getAllFilms();
}