package ru.yandex.practicum.filmorate.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.event.EventDto;
import ru.yandex.practicum.filmorate.model.Event;

@UtilityClass
public class EventMapper {
    public static EventDto mapToEventDto(Event event) {
        return EventDto.builder()
                .timestamp(event.getTimestamp())
                .userId(event.getUserId())
                .eventType(event.getEventType())
                .operation(event.getOperation())
                .eventId(event.getEventId())
                .entityId(event.getEntityId())
                .build();
    }
}

