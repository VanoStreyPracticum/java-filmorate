package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.Review;

public class ReviewMapper {
    public static Review mapToReview(NewReviewRequest newReview) {
        return Review.builder()
                .content(newReview.getContent())
                .isPositive(newReview.getIsPositive())
                .userId(newReview.getUserId())
                .filmId(newReview.getFilmId())
                .build();
    }

    public static Review mapToReview(Review review, UpdateReviewRequest updateReview) {
        if (updateReview.hasContent()) {
            review.setContent(updateReview.getContent());
        }
        if (updateReview.hasIsPositive()) {
            review.setIsPositive(updateReview.getIsPositive());
        }
        if (updateReview.hasUserId()) {
            review.setUserId(updateReview.getUserId());
        }
        if (updateReview.hasFilmId()) {
            review.setFilmId(updateReview.getFilmId());
        }
        return review;
    }

    public static ReviewDto mapToReviewDto(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .content(review.getContent())
                .isPositive(review.getIsPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .useful(review.getUseful())
                .build();
    }

}
