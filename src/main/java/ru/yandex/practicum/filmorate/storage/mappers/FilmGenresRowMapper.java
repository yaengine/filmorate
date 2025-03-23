package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmGenresRowMapper implements RowMapper<Long> {
    @Override
    public Long mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getLong("genre_id");
        }
}
