package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long filmIdCounter = 1;

    @Override
    public Film addFilm(Film film) {
        film.setId(filmIdCounter++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> getFilm(long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public boolean existsFilm(long id) {
        return films.containsKey(id);
    }

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }
}