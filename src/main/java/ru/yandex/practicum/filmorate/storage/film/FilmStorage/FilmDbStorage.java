package ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.directory.DirectoryStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Film> mapper;
    private final RowMapper<Long> filmGenresRowMapper;
    private final RowMapper<Long> filmLikesRowMapper;
    private final DirectoryStorage directoryStorage;
    private final UserDbStorage userDbStorage;

    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films(film_name, description, release_date, " +
            "duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_FILM_GENRE = "INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET film_name = ?, description = ?, release_date = ?, " +
            "duration = ?, mpa_rating_id = ? WHERE film_id = ?";
    private static final String FIND_FILM_GENRES_QUERY = "SELECT genre_id FROM FILM_GENRE WHERE film_id = ? " +
            "ORDER BY genre_id";
    private static final String ADD_LIKE = "INSERT INTO LIKES (film_id, user_id) " +
            "VALUES(?, ?)";
    private static final String REMOVE_LIKE = "DELETE FROM LIKES " +
            "WHERE film_id = ? and user_id = ?";
    private static final String GET_LIKES_USERS_BY_FILM_ID = "SELECT user_id FROM LIKES " +
            "WHERE film_id = ?";

    @Override
    public Collection<Film> findAll() {
        Collection<Film> films = jdbc.query(FIND_ALL_QUERY, mapper);
        for (Film film : films) {
            addGenresAndMpa(film);
            addLikes(film);
        }
        return films;
    }

    @Override
    public Film create(Film film) {
        addGenresAndMpa(film);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        // Возвращаем id
        if (id == null) {
            throw new InternalServerException("Не удалось сохранить данные");
        }

        film.setId(id);

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbc.update(INSERT_FILM_GENRE, film.getId(), genre.getId());
            }
        }

        return film;
    }

    private void addGenresAndMpa(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != 0) {
            film.setMpa(directoryStorage.findMpaById(film.getMpa().getId()));
        }
        if (film.getId() != null) { //ищем в базе
           Set<Long> genreIds = findGenresByFilmId(film.getId());
           if (genreIds != null) {
               Set<Genre> genres = new HashSet<>();
               for (Long genreId : genreIds) {
                   genres.add(Genre.builder().id(genreId).build());
               }
               film.setGenres(genres);
           }
        }
        if (film.getGenres() != null) {
            TreeSet<Genre> genres = new TreeSet<>(Comparator.comparingLong(Genre::getId));
            for (Genre genre : film.getGenres()) {
                genres.add(directoryStorage.findGenreById(genre.getId()));
            }

            film.setGenres(genres);
        }
    }

    private void addLikes(Film film) {
        if (film.getId() != null) {
            Set<Long> usersLikes = new HashSet<>();
            try {
                usersLikes = new HashSet<>(jdbc.query(GET_LIKES_USERS_BY_FILM_ID,
                        new Object[]{film.getId()},
                        filmLikesRowMapper));
            } catch (EmptyResultDataAccessException ignored) {
                log.info("У фильма с id = {} нет лайков", film.getId());
            }

            if (usersLikes != null) {
                film.setLikes(usersLikes);
            }
        }
    }

    @Override
    public Film update(Film newFilm) {
        int rowsUpdated = jdbc.update(UPDATE_QUERY,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa().getId(),
                newFilm.getId());
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        addGenresAndMpa(newFilm);
        return newFilm;
    }

    @Override
    public Film findFilmById(long filmId) {
        try {
            Film film = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, filmId);
            addLikes(film);
            return film;
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
    }

    @Override
    public void addLike(long filmId, long userId) {
       Film film = findFilmById(filmId);
       User user = userDbStorage.findUserById(userId);

       jdbc.update(ADD_LIKE, film.getId(), user.getId());
    }

    @Override
    public void removeLike(long filmId, long userId) {
        Film film = findFilmById(filmId);
        User user = userDbStorage.findUserById(userId);

        jdbc.update(REMOVE_LIKE, film.getId(), user.getId());
    }

    private Set<Long> findGenresByFilmId(Long filmId) {
        try {
            return new HashSet<>(jdbc.query(FIND_FILM_GENRES_QUERY,
                    new Object[]{filmId},
                    filmGenresRowMapper));
        } catch (EmptyResultDataAccessException ignored) {
            log.info("У фильма с id = {} нет жанров", filmId);
            return null;
        }
    }
}
