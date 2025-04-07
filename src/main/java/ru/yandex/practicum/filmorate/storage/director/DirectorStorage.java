package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class DirectorStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Director> directorMapper;

    private static final String FIND_ALL_DIRECTORS_QUERY = "SELECT * FROM directors order by director_id";
    private static final String FIND_DIRECTOR_BY_ID_QUERY = "SELECT * FROM directors WHERE director_id = ?";
    private static final String CREATE_DIRECTOR = "INSERT INTO directors (director_name) VALUES(?)";
    private static final String UPDATE_DIRECTOR = "UPDATE directors SET director_name = ? WHERE director_id = ?";
    private static final String DELETE_DIRECTOR = "DELETE FROM directors WHERE director_id = ?";

    public Collection<Director> findAllDirectors() {
        return jdbc.query(FIND_ALL_DIRECTORS_QUERY, directorMapper);
    }

    public Director findDirectorById(long directorId) {
        try {
            return jdbc.queryForObject(FIND_DIRECTOR_BY_ID_QUERY, directorMapper, directorId);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Режисер с id = " + directorId + " не найден");
        }
    }

    public Director create(Director director) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(CREATE_DIRECTOR, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        // Возвращаем id
        if (id == null) {
            throw new InternalServerException("Не удалось сохранить данные");
        }

        director.setId(id);
        return  director;
    }

    public Director update(Director director) {
        int rowsUpdated = jdbc.update(UPDATE_DIRECTOR,
                director.getName(),
                director.getId());
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        return director;
    }

    public void delete(long directorId) {
        jdbc.update(DELETE_DIRECTOR, directorId);
    }
}
