package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDTO;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;
    private final MpaMapper mpaMapper;

    @GetMapping
    public ResponseEntity<List<MpaDTO>> getAll() {
        List<MpaDTO> response = mpaService.getAll()
                .stream()
                .map(mpaMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MpaDTO> getById(@PathVariable int id) {
        Mpa mpa = mpaService.getById(id);
        return ResponseEntity.ok(mpaMapper.toDTO(mpa));
    }
}