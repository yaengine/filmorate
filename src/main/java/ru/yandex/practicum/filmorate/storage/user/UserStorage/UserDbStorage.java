package ru.yandex.practicum.filmorate.storage.user.UserStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<User> mapper;
    private final @Qualifier("frendIdsRowMapper") RowMapper<Long> frendIdsRowMapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_FRIENDS_BY_ID = "SELECT f.friend_id from FRIENDSHIPS f \n" +
            "WHERE f.user_id = ? ";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(user_name, email, login, birthday) " +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET user_name = ?, email = ?, login = ?, birthday = ?  " +
            "WHERE user_id = ?";
    private static final String ADD_FRIEND = "INSERT INTO friendships (user_id, friend_id, status) " +
            "VALUES (?, ?, FALSE) ";
    private static final String REMOVE_FRIEND = "DELETE FROM FRIENDSHIPS f \n" +
            "WHERE f.user_id = ? AND f.friend_id = ? ";
    private static final String CHECK_FRIEND_STATUS = "UPDATE friendships AS f \n" +
            "SET STATUS = CASE WHEN (select count(*) from friendships f1 \n" +
                              "WHERE (f1.user_id = :1 AND f1.friend_id = :2) \n" +
                                 "OR (f1.friend_id = :1 AND f1.user_id = :2)) = 2 THEN TRUE \n" +
                              "ELSE FALSE END \n" +
            "WHERE (f.user_id = :1 AND f.friend_id = :2)\n" +
            "OR (f.friend_id = :1 AND f.user_id = :2) ";
    private static final String DELETE_USER = "DELETE FROM USERS WHERE user_id = ?";

    @Override
    public Collection<User> findAll() {
        Collection<User> users = jdbc.query(FIND_ALL_QUERY, mapper);
        for (User user : users) {
            user.setFriends(findFriendByUserId(user.getId()));
        }
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    public User findUserById(long userId) {
        try {
            User user = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, userId);
            if (user != null) {
                Set<Long> friendIds = findFriendByUserId(user.getId());
                if (friendIds != null) {
                    user.setFriends(friendIds);
                }
            } else {
                throw new EmptyResultDataAccessException(0);
            }
            return user;
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    private Set<Long> findFriendByUserId(Long userId) {
        try {
            return new HashSet<>(jdbc.query(FIND_FRIENDS_BY_ID,
                                            new Object[]{userId},
                                            frendIdsRowMapper));
        } catch (EmptyResultDataAccessException ignored) {
            log.info("У пользователя с id = {} нет друзей", userId);
            return null;
        }
    }

    @Override
    public User create(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getLogin());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        // Возвращаем id
        if (id == null) {
            throw new InternalServerException("Не удалось сохранить данные");
        }

        user.setId(id);
        return user;
    }

    public User update(User user) {
        int rowsUpdated = jdbc.update(UPDATE_QUERY,
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getBirthday(),
                user.getId());
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        return user;
    }

    @Override
    public void addFriend(long userId, long friendId) {
        jdbc.update(ADD_FRIEND,
                    userId,
                    friendId);
        checkFriendStatus(userId, friendId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        jdbc.update(REMOVE_FRIEND,
                userId,
                friendId);
    }

    @Override
    public void deleteUser(long id) {
        int affectedRows = jdbc.update(DELETE_USER, id);
        if (affectedRows == 0) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
}

    private void checkFriendStatus(long userId, long friendId) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbc);
        Map<String, Object> params = new HashMap<>();
        params.put("1", userId);
        params.put("2", friendId);
        namedParameterJdbcTemplate.update(CHECK_FRIEND_STATUS, params);
    }
}
