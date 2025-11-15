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
    String eventType;   // одно из значениий LIKE, REVIEW или FRIEND
    String operation;   // одно из значениий REMOVE, ADD, UPDATE
    Long eventId;       //primary key
    Long entityId;     // идентификатор сущности, с которой произошло событие
}