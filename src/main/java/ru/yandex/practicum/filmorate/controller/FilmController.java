package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public ResponseEntity<Film> createFilm(@Valid @RequestBody Film film) {
        Film created = filmService.create(film);
        log.info("Добавлен фильм: id={}, name='{}'", created.getId(), created.getName());
        return ResponseEntity.ok(created);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        Film updated = filmService.update(film);
        log.info("Обновлён фильм: id={}, name='{}'", updated.getId(), updated.getName());
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<Collection<Film>> getAllFilms() {
        return ResponseEntity.ok(filmService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@PathVariable long id) {
        return ResponseEntity.ok(filmService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilm(@PathVariable long id) {
        filmService.delete(id);
        log.info("Удалён фильм: id={}", id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<Film>> getPopularFilms(@RequestParam(defaultValue = "10") Integer count,
                                            @RequestParam(required = false) Integer genreId,
                                            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(filmService.getPopularFilms(count, genreId, year));
    }

    @GetMapping("/common")
    public ResponseEntity<Collection<Film>> getCommonFilms(@RequestParam(defaultValue = "10") Integer count,
                                            @RequestParam(required = false) Long userId,
                                            @RequestParam(required = false) Long friendId) {
        return ResponseEntity.ok(filmService.getCommonFilms(userId, friendId));
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<Collection<Film>> getFilmsByDirector(@PathVariable int directorId, @RequestParam String sortBy) {
        return ResponseEntity.ok(filmService.getFilmsByDirector(directorId, sortBy));
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<Film>> searchFilms(@RequestParam String query,
                                        @RequestParam String by) {
        return ResponseEntity.ok(filmService.searchFilms(query, by));
    }
}