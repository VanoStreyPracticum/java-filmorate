package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ValidationService;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;
    private final ValidationService validationService;

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validationService.validateNewFilm(film);
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        validationService.validateNewFilm(film);
        return filmService.update(film);
    }

    @GetMapping
    public List<Film> findAll() {
        return filmService.findAll();
    }
}
