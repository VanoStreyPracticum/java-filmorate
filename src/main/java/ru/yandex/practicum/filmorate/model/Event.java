package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Event {
    Long timestamp;
    Long userId;
    String eventType;
    String operation;
    Long eventId;
    Long entityId;
}