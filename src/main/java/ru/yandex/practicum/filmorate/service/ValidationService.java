package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;

@Service
public class ValidationService {
    private static final LocalDate FIRST_PUBLIC_SCREENING = LocalDate.of(1895, 12, 28);

    public void validateNewFilm(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_PUBLIC_SCREENING)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность должна быть положительной");
        }
    }

    public void validateNewUser(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    public void validateNewReview(NewReviewRequest newReview) {
        if (newReview.getContent() == null || newReview.getContent().isBlank()) {
            throw new ValidationException("Отзыв не может быть пустым");
        }
        if (newReview.getIsPositive() == null) {
            throw new ValidationException("Отзыв должен быть или положительным или отрицательным");
        }
        if (newReview.getFilmId() == null) {
            throw new ValidationException("ID фильма не может быть пустым");
        }
        if (newReview.getFilmId() <= 0) {
            throw new NotFoundException("ID фильма не может быть меньше 1");
        }
        if (newReview.getUserId() == null) {
            throw new ValidationException("ID пользователя не может быть пустым");
        }
        if (newReview.getUserId() <= 0) {
            throw new NotFoundException("ID пользователя не может быть меньше 1");
        }
    }
}
