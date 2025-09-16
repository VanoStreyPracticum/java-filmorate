package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Film> films = new HashMap<>();

    private long userIdCounter = 1;
    private long filmIdCounter = 1;

    // ---------- USERS ----------
    public User addUser(User user) {
        user.setId(userIdCounter++);
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> getUser(long id) {
        return Optional.ofNullable(users.get(id));
    }

    public boolean existsUser(long id) {
        return users.containsKey(id);
    }

    public Collection<User> getAllUsers() {
        return users.values();
    }

    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public void deleteUser(long id) {
        users.remove(id);
    }

    // ---------- FILMS ----------
    public Film addFilm(Film film) {
        film.setId(filmIdCounter++);
        films.put(film.getId(), film);
        return film;
    }

    public Optional<Film> getFilm(long id) {
        return Optional.ofNullable(films.get(id));
    }

    public boolean existsFilm(long id) {
        return films.containsKey(id);
    }

    public Collection<Film> getAllFilms() {
        return films.values();
    }

    public Film updateFilm(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    public void deleteFilm(long id) {
        films.remove(id);
    }
}
