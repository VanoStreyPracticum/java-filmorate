package ru.yandex.practicum.filmorate.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class EventDto {
    Long timestamp;
    Long userId;
    String eventType;
    String operation;
    Long eventId;
    Long entityId;
}