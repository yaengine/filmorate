package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.mappers.FeedRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FeedDbStorage {

    private final JdbcTemplate jdbc;
    private final FeedRowMapper feedRowMapper;

    public List<Feed> findAllFeeds() {
        String query = "SELECT event_id, user_id, entity_id, time_stamp, event_type, operation FROM feed";
        List<Feed> feeds = jdbc.query(query, feedRowMapper);
        log.info("Получены все события {}.", feeds);
        return feeds;
    }

    public Optional<Feed> getFeedById(Long id) {
        String query = "SELECT event_id, user_id, entity_id, time_stamp, event_type, operation FROM feed WHERE event_id = ?";
        try {
            Feed result = jdbc.queryForObject(query, feedRowMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Событие с id " + id + " не найдено");
        }
    }

    public void createFeed(Long userId, Long entityId, EventType eventType, Operation operation) {
        Feed feed = Feed.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .entityId(entityId)
                .eventType(eventType)
                .operation(operation)
                .build();
        String sqlQuery = "INSERT INTO feed(user_id, entity_id, time_stamp, event_type, operation) VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, feed.getUserId());
            statement.setLong(2, feed.getEntityId());
            statement.setLong(3, feed.getTimestamp());
            statement.setString(4, feed.getEventType().toString());
            statement.setString(5, feed.getOperation().toString());
            return statement;
        }, keyHolder);
        Long feedId;
        if (Objects.nonNull(keyHolder.getKey())) {
            feedId = keyHolder.getKey().longValue();
        } else {
            throw new NotFoundException("Ошибка присвоения id событию");
        }
        log.info("Создано новое событие с id {}, userId {}, entityId {}, eventType {}, operation {}", feedId, userId, entityId, eventType, operation);
        getFeedById(feedId).orElseThrow();
    }

    public List<Feed> getFeedByUser(Long userId) {
        String query = "SELECT DISTINCT event_id, user_id, entity_id, time_stamp, event_type, operation " +
                "FROM feed " +
                "WHERE user_id = ? " +
                "ORDER BY time_stamp";
        List<Feed> feeds = jdbc.query(query, feedRowMapper, userId);
        log.info("Получены события для пользователя с id {}", userId);
        return feeds;
    }
}



