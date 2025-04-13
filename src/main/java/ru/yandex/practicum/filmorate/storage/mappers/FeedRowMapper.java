package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedRowMapper implements RowMapper<Feed> {

    public Feed mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        String event = resultSet.getString("event_type").toUpperCase();
        String operation = resultSet.getString("operation").toUpperCase();

        return Feed.builder()
                .eventId(resultSet.getLong("event_id"))
                .entityId(resultSet.getLong("entity_id"))
                .userId(resultSet.getLong("user_id"))
                .timestamp(resultSet.getLong("time_stamp"))
                .eventType(getEvent(event))
                .operation(getOperation(operation))
                .build();
    }

    private EventType getEvent(String event) {
        try {
            return EventType.valueOf(event);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Событие не найдено: " + event);
        }
    }

    private Operation getOperation(String operation) {
        try {
            return Operation.valueOf(operation);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Операция не найдена: " + operation);
        }
    }
}

