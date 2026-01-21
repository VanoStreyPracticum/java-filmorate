package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.NewDirector;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirector;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public ResponseEntity<List<DirectorDto>> getAll() {
        return ResponseEntity.ok(directorService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DirectorDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(directorService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DirectorDto> create(@RequestBody @Valid NewDirector newDirector) {
        return ResponseEntity.ok(directorService.create(newDirector));
    }


    @PutMapping
    public ResponseEntity<DirectorDto> update(@RequestBody UpdateDirector updateDirector) {
        return ResponseEntity.ok(directorService.update(updateDirector));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        directorService.delete(id);
    }
}
