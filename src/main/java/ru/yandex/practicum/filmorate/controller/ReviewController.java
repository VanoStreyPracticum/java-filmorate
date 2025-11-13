package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ReviewDto create(@RequestBody NewReviewRequest newReview) {
        log.info("Создание нового отзыва {}", newReview);
        return reviewService.create(newReview);
    }

    @PostMapping
    public ReviewDto update(@RequestBody UpdateReviewRequest updateReview) {
        log.info("Обновление нового отзыва {}", updateReview);
        return reviewService.update(updateReview);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Удаление отзыва по ID {}", id);
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public ReviewDto getById(@PathVariable Long id) {
        log.info("Получение отзыва по ID {}", id);
        return reviewService.findById(id);
    }

    @GetMapping
    public List<ReviewDto> getMany(@RequestParam Long filmId, @RequestParam Long count) {
        log.info("Получение отзывов к отзыву по ID {} в количестве {}", filmId, count);
        return reviewService.findMany(filmId, count);
    }

    @PutMapping("{id}/like/{userId}")
    public void createLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Создание лайка к отзыву по ID {} от пользователя по ID {}", id, userId);
        reviewService.createLike(id, userId, true);
    }

    @PutMapping("{id}/dislike/{userId}")
    public void createDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Создание дизлайка к отзыву по ID {} от пользователя по ID {}", id, userId);
        reviewService.createLike(id, userId, false);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка к отзыву по ID {} от пользователя по ID {}", id, userId);
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление дизлайка к отзыву по ID {} от пользователя по ID {}", id, userId);
        reviewService.removeLike(id, userId);
    }
}
