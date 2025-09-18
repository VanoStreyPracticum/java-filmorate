package ru.yandex.practicum.filmorate.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.ValidationService;
import ru.yandex.practicum.filmorate.storage.InMemoryStorage;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {
    private final InMemoryStorage storage;
    private final ValidationService validationService;

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        validationService.validateNewFilm(film);
        Film saved = storage.addFilm(film);
        log.info("Добавлен фильм: id={}, name='{}'", saved.getId(), saved.getName());
        return ResponseEntity.ok(saved);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        if (film.getId() == null || !storage.existsFilm(film.getId())) {
            String msg = "Фильм с указанным id не найден";
            log.warn(msg + " id=" + film.getId());
            return ResponseEntity.notFound().build();
        }
        validationService.validateNewFilm(film);
        Film updated = storage.addFilm(film);
        log.info("Обновлён фильм: id={}, name='{}'", updated.getId(), updated.getName());
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public Collection<Film> getAll() {
        return storage.getAllFilms();
    }
}
