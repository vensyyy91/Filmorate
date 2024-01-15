package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.util.Mapper;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventDaoImpl implements EventDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void writeEvent(int userId, EventType eventType, Operation operation, int entityId) {
        String sql = "INSERT INTO events (create_timestamp, user_id, event_type, operation, entity_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, Instant.now(), userId, eventType.name(), operation.name(), entityId);
    }

    @Override
    public List<Event> getUserEvents(int id) {
        String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY create_timestamp";

        return jdbcTemplate.query(sql, Mapper::makeEvent, id);
    }
}