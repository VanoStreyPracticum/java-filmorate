package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ValidationService;
import ru.yandex.practicum.filmorate.storage.InMemoryStorage;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final InMemoryStorage storage;
    private final ValidationService validationService;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        validationService.validateNewUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User saved = storage.addUser(user);
        log.info("Создан пользователь: id={}, login='{}'", saved.getId(), saved.getLogin());
        return ResponseEntity.ok(saved);
    }

    // UserController.java
    @PutMapping
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        if (user.getId() == null || !storage.existsUser(user.getId())) {
            String msg = "Пользователь с указанным id не найден: " + user.getId();
            log.warn(msg);
            throw new NotFoundException(msg);
        }
        validationService.validateNewUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        User updated = storage.updateUser(user);
        log.info("Обновлён пользователь: id={}, login='{}'", updated.getId(), updated.getLogin());
        return ResponseEntity.ok(updated);
    }


    @GetMapping
    public Collection<User> getAllUsers() {
        return storage.getAllUsers();
    }
}
