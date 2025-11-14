package ru.yandex.practicum.filmorate.dto.review;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewReviewRequest {
    String content;
    Boolean isPositive;
    Long userId;
    Long filmId;
}
