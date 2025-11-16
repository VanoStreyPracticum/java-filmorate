package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventStorage {
    public Event create(Event event);

    public List<Event> getEventsByUserId(Long userId);
}