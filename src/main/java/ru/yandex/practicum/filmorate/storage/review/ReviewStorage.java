package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    public Review create(Review review);

    public Review update(Review review);

    public void delete(Long id);

    public Review findById(Long id);

    public List<Review> findMany(Long filmId, Long resultSize);

    public void createLike(Long reviewId, Long userId, boolean isPositive);

    public void removeLike(Long reviewId, Long userId);
}
