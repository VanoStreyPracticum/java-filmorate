package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.event.EventDto;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class EventService {
    @Qualifier("eventDbStorage")
    private final EventStorage eventStorage;

    public EventService(EventStorage eventStorage) {
        this.eventStorage = eventStorage;
    }

    public List<EventDto> getUserFeed(Long userId) {
        log.info("Получение ленты событий по пользователю с ID {}", userId);
        List<Event> events = eventStorage.getEventsByUserId(userId);
        log.info("Лента событий по пользователю с ID {} получена {}", userId, events);
        return events.stream()
                .map(EventMapper::mapToEventDto)
                .toList();
    }


    public void createEvent(Long userId, String eventType, String operation, Long entityId) {
        log.info("Создание события userId {} eventType {} operation {} entityId {}",
                userId, eventType, operation, entityId);
        Event event = Event.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .build();
        event = eventStorage.create(event);
        log.info("В БД добавлено событие {}", event);
    }
}