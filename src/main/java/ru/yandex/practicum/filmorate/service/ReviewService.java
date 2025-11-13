package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final ValidationService validationService;

    public ReviewService(@Qualifier("reviewDbStorage") ReviewStorage reviewStorage,
                         ValidationService validationService) {
        this.reviewStorage = reviewStorage;
        this.validationService = validationService;
    }

    public ReviewDto create(NewReviewRequest newReview) {
        log.info("Создание нового отзыва {}", newReview);
        validationService.validateNewReview(newReview);
        log.info("Прошел валидацию отзыв {}", newReview);
        Review review = ReviewMapper.mapToReview(newReview);
        log.info("После маппинга отзыв {}", review);
        review = reviewStorage.create(review);
        log.info("Был создан новый отзыв {}", review);
        return ReviewMapper.mapToReviewDto(review);
    }

    public ReviewDto update(UpdateReviewRequest updateRequest) {
        log.info("Обновление отзыва {}", updateRequest);
        Review reviewFromDb = reviewStorage.findById(updateRequest.getReviewId());
        log.info("Получен отзыв из БД {}", reviewFromDb);
        Review mappedReview = ReviewMapper.mapToReview(reviewFromDb, updateRequest);
        log.info("Отзыв после маппинга новых полей {}", mappedReview);
        Review updatedReview = reviewStorage.update(mappedReview);
        log.info("Обновленный отзыв в базе данных {}", updatedReview);
        return ReviewMapper.mapToReviewDto(updatedReview);
    }

    public void delete(Long id) {
        log.info("Удаление отзыва с ID {}", id);
        reviewStorage.delete(id);
        log.info("Успешно удален отзыв с ID {}", id);
    }

    public ReviewDto findById(Long id) {
        log.info("Получение отзыва по ID {}", id);
        Review reviewFromDb = reviewStorage.findById(id);
        log.info("Успешно получен отзыв по ID {}", reviewFromDb);
        return ReviewMapper.mapToReviewDto(reviewFromDb);
    }

    public List<ReviewDto> findMany(Long filmId, Long resultSize) {
        log.info("Получение отзывов к фильмам с ID {} длиной {}", filmId, resultSize);
        List<Review> reviewList = reviewStorage.findMany(filmId, resultSize);
        log.info("К фильму с ID {} получен список отзывов {}", filmId, reviewList);
        return reviewList.stream()
                .map(ReviewMapper::mapToReviewDto)
                .toList();
    }

    public void createLike(Long reviewId, Long userId, boolean isPositive) {
        log.info("Создание лайка {} к отзыву с ID {} от пользователя с ID {}", isPositive, reviewId, userId);
        reviewStorage.createLike(reviewId, userId, isPositive);
        log.info("Успешно создан лайк {} к отзыву с ID {} от пользователя с ID {}", isPositive, reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        log.info("Удаление лайка у отзыва с ID {} от пользователя с ID {}", reviewId, userId);
        reviewStorage.removeLike(reviewId, userId);
        log.info("Успешно удален лайк к отзыву с ID {} от пользователя с ID {}", reviewId, userId);
    }
}
