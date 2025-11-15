package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
@Qualifier("eventDbStorage")
public class EventDbStorage implements EventStorage {
    private static final String INSERT_EVENT_QUERY = "INSERT INTO events (timestamp, user_id, event_type, " +
            "operation, entity_id) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_EVENT_QUERY = "SELECT * FROM events WHERE user_id = ?";

    private final JdbcTemplate jdbc;
    private final EventRowMapper rowMapper;

    @Override
    public Event create(Event event) {
        log.info("Вставка объекта {}", event);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(INSERT_EVENT_QUERY,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setObject(1, Timestamp.valueOf(event.getTimestamp()));
                ps.setObject(2, event.getUserId());
                ps.setObject(3, event.getEventType());
                ps.setObject(4, event.getOperation());
                ps.setObject(5, event.getEntityId());
                return ps;
            }, keyHolder);

            Long id = keyHolder.getKeyAs(Long.class);
            if (id != null) {
                event.setEventId(id);
                return event;
            } else {
                log.error("Не удалось сохранить данные");
                throw new RuntimeException("Не удалось сохранить данные");
            }
        } catch (DataIntegrityViolationException ex) {
            log.error("Попытка вставки данных в базу с неверным ID");
            throw new NotFoundException("Попытка вставки данных в базу с неверным ID");
        }
    }

    @Override
    public List<Event> getEventsByUserId(Long userId) {
        log.error("Попытка получения листа событий по user_id {}", userId);
        return jdbc.query(SELECT_EVENT_QUERY, rowMapper, userId);
    }
}