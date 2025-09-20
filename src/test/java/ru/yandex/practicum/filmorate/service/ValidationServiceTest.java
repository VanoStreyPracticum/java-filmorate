package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {
    private final ValidationService service = new ValidationService();

    @Test
    void filmReleaseTooEarly() {
        Film f = Film.builder()
                .name("Old film")
                .description("desc")
                .releaseDate(LocalDate.of(1800,1,1))
                .duration(100).build();
        assertThrows(ValidationException.class, () -> service.validateNewFilm(f));
    }

    @Test
    void filmDurationNonPositive() {
        Film f = Film.builder()
                .name("X")
                .description("d")
                .releaseDate(LocalDate.of(2000,1,1))
                .duration(0).build();
        assertThrows(ValidationException.class, () -> service.validateNewFilm(f));
    }

    @Test
    void userLoginWithSpace() {
        User u = User.builder()
                .email("a@b.com")
                .login("bad login")
                .birthday(LocalDate.of(1990,1,1)).build();
        assertThrows(ValidationException.class, () -> service.validateNewUser(u));
    }

    @Test
    void userBirthdayInFuture() {
        User u = User.builder()
                .email("a@b.com")
                .login("good")
                .birthday(LocalDate.now().plusDays(1)).build();
        assertThrows(ValidationException.class, () -> service.validateNewUser(u));
    }
}

