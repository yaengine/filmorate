package ru.yandex.practicum.filmorate.storage.directory;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class DirectoryStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Genre> genreMapper;
    private final RowMapper<Mpa> mpaMapper;

    private static final String FIND_ALL_GENRES_QUERY = "SELECT * FROM genres order by genre_id";
    private static final String FIND_GENRE_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String FIND_ALL_MPA_QUERY = "SELECT * FROM mpa_ratings order by mpa_rating_id";
    private static final String FIND_MPA_BY_ID_QUERY = "SELECT * FROM mpa_ratings WHERE mpa_rating_id = ?";

    public Collection<Genre> findAllGenres() {
        return jdbc.query(FIND_ALL_GENRES_QUERY, genreMapper);
    }

    public Genre findGenreById(long genreId) {
        try {
            return jdbc.queryForObject(FIND_GENRE_BY_ID_QUERY, genreMapper, genreId);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Жанр с id = " + genreId + " не найден");
        }
    }

    public Collection<Mpa> findAllMpa() {
        return jdbc.query(FIND_ALL_MPA_QUERY, mpaMapper);
    }

    public Mpa findMpaById(long mpaId) {
        try {
            return jdbc.queryForObject(FIND_MPA_BY_ID_QUERY, mpaMapper, mpaId);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Рейтинг с id = " + mpaId + " не найден");
        }
    }
}
