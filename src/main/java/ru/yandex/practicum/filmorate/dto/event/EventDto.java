package ru.yandex.practicum.filmorate.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class EventDto {
    LocalDateTime timestamp;
    Long userId;
    String eventType;
    String operation;
    Long eventId;
    Long entityId;
}