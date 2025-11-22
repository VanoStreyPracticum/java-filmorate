package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.film.FilmDTO;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public ResponseEntity<FilmDTO> createFilm(@Valid @RequestBody NewFilmRequest newFilmRequest) {
        FilmDTO created = filmService.create(newFilmRequest);
        return ResponseEntity.ok(created);
    }

    @PutMapping
    public ResponseEntity<FilmDTO> updateFilm(@Valid @RequestBody UpdateFilmRequest updateFilmRequest) {
        FilmDTO updated = filmService.update(updateFilmRequest);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<Collection<FilmDTO>> getAllFilms() {
        return ResponseEntity.ok(filmService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDTO> getFilmById(@PathVariable long id) {
        return ResponseEntity.ok(filmService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilm(@PathVariable long id) {
        filmService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable long id, @PathVariable long userId) {
        filmService.addLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable long id, @PathVariable long userId) {
        filmService.removeLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<FilmDTO>> getPopularFilms(@RequestParam(defaultValue = "10") Integer count,
                                                               @RequestParam(required = false) Integer genreId,
                                                               @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(filmService.getPopularFilms(count, genreId, year));
    }

    @GetMapping("/common")
    public ResponseEntity<Collection<FilmDTO>> getCommonFilms(@RequestParam(defaultValue = "10") Integer count,
                                                              @RequestParam(required = false) Long userId,
                                                              @RequestParam(required = false) Long friendId) {
        return ResponseEntity.ok(filmService.getCommonFilms(userId, friendId));
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<Collection<FilmDTO>> getFilmsByDirector(@PathVariable int directorId,
                                                                  @RequestParam String sortBy) {
        return ResponseEntity.ok(filmService.getFilmsByDirector(directorId, sortBy));
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<FilmDTO>> searchFilms(@RequestParam String query,
                                                           @RequestParam String by) {
        return ResponseEntity.ok(filmService.searchFilms(query, by));
    }
}