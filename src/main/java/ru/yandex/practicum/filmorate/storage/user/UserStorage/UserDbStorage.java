package ru.yandex.practicum.filmorate.storage.user.UserStorage;

import lombok.RequiredArgsConstructor;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<User> mapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(user_name, email, login, birthday) " +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET user_name = ?, email = ?, login = ?, birthday = ?  " +
            "WHERE user_id = ?";
    private static final String ADD_FRIENDS = "INSERT INTO friendships (user_id, friend_id, status) " +
            "VALUES (?, ?, false) ON CONFLICT DO NOTHING; ";
    private static final String CHECK_FRIENDSHIP_STATUS =
            "UPDATE friendships f " +
            "SET STATUS = CASE WHEN (select count(*) " +
                                    "  from friendships f1" +
                                    " WHERE (f1.user_id = :1 AND f1.friend_id = :2) " +
                                    "    OR (f1.user_id = :2 AND f1.friend_id = :1)) = 2 THEN TRUE " +
                            "  ELSE FALSE END " +
            "WHERE (f.user_id = :1 AND f.friend_id = :2) " +
            "   OR (f.user_id = :2 AND f.friend_id = :1) ";

    @Override
    public Collection<User> findAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    public User findUserById(long userId) {
        try {
            return jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, userId);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

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
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbc);
        jdbc.update(ADD_FRIENDS,
                    userId,
                    friendId);

        Map<String, Object> params = new HashMap<>();
        params.put("1", userId);
        params.put("2", friendId);
        namedParameterJdbcTemplate.update(CHECK_FRIENDSHIP_STATUS, params);
    }
}
