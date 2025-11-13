package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository("reviewDbStorage")
public class ReviewDbStorage implements ReviewStorage {
    private static final int QUERY_SIZE_BY_DEFAULT = 10;

    private static final String INSERT_QUERY = "INSERT INTO reviews(content, is_positive, user_id, film_id) " +
            "VALUES (?, ?, ?, ?)";

    private static final String UPDATE_QUERY = "UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, " +
            "film_id = ? WHERE id = ?";

    private static final String DELETE_QUERY = "DELETE FROM reviews WHERE id = ?";

    private static final String FIND_BY_ID_QUERY = "SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id, " +
            "COALESCE(agg.like_count, 0) AS like_count, " +
            "COALESCE(agg.dislike_count, 0) AS dislike_count " +
            "FROM reviews r " +
            "LEFT JOIN ( SELECT review_id,  " +
            "SUM(CASE WHEN is_like THEN 1 ELSE 0 END) AS like_count, " +
            "SUM(CASE WHEN NOT is_like THEN 1 ELSE 0 END) AS dislike_count " +
            "FROM review_ratings GROUP BY review_id ) agg ON r.id = agg.review_id " +
            "WHERE r.id = ?";

    private static final String FIND_ALL_QUERY = "SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id, " +
            "COALESCE(agg.like_count, 0) AS like_count, " +
            "COALESCE(agg.dislike_count, 0) AS dislike_count " +
            "FROM reviews r " +
            "LEFT JOIN (SELECT review_id,  " +
            "SUM(CASE WHEN is_like THEN 1 ELSE 0 END) AS like_count, " +
            "SUM(CASE WHEN NOT is_like THEN 1 ELSE 0 END) AS dislike_count " +
            "FROM review_ratings GROUP BY review_id) agg ON r.id = agg.review_id " +
            "WHERE r.film_id = COALESCE(?, r.film_id) " +
            "ORDER BY (COALESCE(agg.like_count, 0) - COALESCE(agg.dislike_count, 0)) DESC  " +
            "LIMIT ?";

    private static final String INSERT_LIKE_QUERY =
            "MERGE INTO review_ratings " +
                    "USING (VALUES (?, ?, ?)) AS src (user_id, review_id, is_like) " +
                    "ON review_ratings.user_id = src.user_id AND review_ratings.review_id = src.review_id " +
                    "WHEN MATCHED THEN " +
                    "  UPDATE SET is_like = src.is_like " +
                    "WHEN NOT MATCHED THEN " +
                    "  INSERT (user_id, review_id, is_like) VALUES (src.user_id, src.review_id, src.is_like)";

    private static final String DELETE_LIKE_QUERY = "DELETE FROM review_ratings WHERE user_id = ? AND review_id = ?";

    protected final JdbcTemplate jdbc;
    protected final RowMapper<Review> mapper;

    @Override
    public Review create(Review review) {
        log.info("Попытка создания объекта {}", review);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(INSERT_QUERY,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setObject(1, review.getContent());
                ps.setObject(2, review.getIsPositive());
                ps.setObject(3, review.getUserId());
                ps.setObject(4, review.getFilmId());
                return ps;
            }, keyHolder);
            Long id = keyHolder.getKeyAs(Long.class);
            log.info("Сгенерирован id {}", id);
            if (id != null) {
                review.setReviewId(id);
            } else {
                log.error("Не удалось сохранить объект {}", review);
                throw new RuntimeException("Не удалось сохранить данные");
            }
        } catch (DataIntegrityViolationException ex) {
            log.error("В базу не добавлен объект {}", review);
            throw new RuntimeException("Не удалось сохранить данные");
        }
        log.error("Успешно добавили в базу объект {}", review);
        return review;
    }

    @Override
    public Review update(Review review) {
        log.info("Попытка обновления объекта {}", review);
        int updateResult = jdbc.update(UPDATE_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getReviewId());
        if (updateResult == 0) {
            log.error("не удалось обновить объект {}", review);
            throw new NotFoundException("Не удалось обновить данные");
        }
        log.info("Успешно обновлен объект {}", review);
        return review;
    }

    @Override
    public void delete(Long id) {
        log.info("Попытка удаления объекта c ID {}", id);
        int deleteResult = jdbc.update(DELETE_QUERY, id);
        if (deleteResult == 0) {
            log.error("Попытка удаления несуществующих данных с ID {}", id);
            throw new NotFoundException("Попытка удаления несуществующих данных");
        }
        log.info("Успешно удален объект c ID {}", id);
    }

    @Override
    public Review findById(Long id) {
        log.info("Попытка получить пользователя по ID {}", id);
        try {
            Review review = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            log.info("Получен пользователь с ID {}", id);
            return review;
        } catch (EmptyResultDataAccessException e) {
            log.error("В базе отсутствует пользователь с ID {}", id);
            throw new NotFoundException("Пользователь с указанным ID отсутствует");
        }
    }

    @Override
    public List<Review> findMany(Long filmId, Long resultSize) {
        log.info("Попытка получить отзывы по фильму с ID {} и размером {}", filmId, resultSize);
        List<Review> reviewList = jdbc.query(FIND_ALL_QUERY, mapper, filmId, resultSize);
        log.info("Коллекция отзывов к фильму с ID {} размером {} получена", filmId, reviewList.size());
        return reviewList;
    }

    @Override
    public void createLike(Long reviewId, Long userId, boolean isPositive) {
        log.info("Создание лайка от пользователя с ID {}, отзыву с ID {} тип лайка {}", userId, reviewId, isPositive);
        try {
            jdbc.update(INSERT_LIKE_QUERY, userId, reviewId, isPositive);
            log.info("Успешно создан лайк от пользователя с ID {}, отзыву с ID {} тип лайка {}",
                    userId, reviewId, isPositive);
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось создать лайк от пользователя с ID {}, отзыву с ID {} тип лайка {}",
                    userId, reviewId, isPositive);
            throw new NotFoundException("Не удалось сохранить данные");
        }
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        log.info("Попытка удаления лайка пользователя c ID {} у отзыва с ID {}", userId, reviewId);
        int deleteResult = jdbc.update(DELETE_LIKE_QUERY, userId, reviewId);
        if (deleteResult == 0) {
            log.error("Попытка удаления несуществующих данных userID {}, reviewId {}", userId, reviewId);
            throw new NotFoundException("Попытка удаления несуществующих данных");
        }
        log.info("Успешно удален лайк пользователя c ID {} у отзыва с ID {}", userId, reviewId);
    }
}
