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
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.FeedDbStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.directory.DirectoryStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage.UserDbStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Film> mapper;
    private final @Qualifier("filmGenresRowMapper") RowMapper<Long> filmGenresRowMapper;
    private final @Qualifier("filmLikesRowMapper") RowMapper<Long> filmLikesRowMapper;
    private final DirectoryStorage directoryStorage;
    private final UserDbStorage userDbStorage;
    private final DirectorStorage directorStorage;
    private final @Qualifier("filmDirectorsRowMapper") RowMapper<Long> filmDirectorsRowMapper;
    private final FeedDbStorage feedDbStorage;

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
    private static final String DELETE_FILM = "DELETE FROM FILMS WHERE film_id = ?";
    private static final String FIND_FILM_DIRECTORS_QUERY = "SELECT director_id FROM FILM_DIRECTORS " +
            "WHERE film_id = ? " +
            "ORDER BY director_id";
    private static final String INSERT_FILM_DIRECTORS = "INSERT INTO FILM_DIRECTORS (FILM_ID, DIRECTOR_ID)" +
            " VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS = "DELETE FROM FILM_DIRECTORS " +
            " WHERE FILM_ID = ?";
    private static final String FIND_FILMS_BY_DIR = "SELECT F.* FROM FILMS F " +
            "JOIN FILM_DIRECTORS FD ON (F.FILM_ID = FD.FILM_ID) " +
            "WHERE FD.DIRECTOR_ID = ? ";
    private static final String FIND_FILMS_BY_ORDER_BY_YEARS = " ORDER BY F.RELEASE_DATE ";
    private static final String FIND_FILMS_BY_ORDER_BY_LIKES = " ORDER BY (SELECT COUNT(*) " +
            " FROM LIKES L " +
            " WHERE L.FILM_ID = F.FILM_ID) DESC";
    private static final String FIND_LIKED_FILMS_IDS = "SELECT film_id FROM likes WHERE user_id = ?";
    private static final String SEARCH_FILMS_BY = "WITH p AS (SELECT CAST(? AS VARCHAR) as query) " +
            "SELECT * FROM FILMS F, P WHERE 1 = 1 ";
    private static final String SEARCH_FILMS_TITLE = " AND lower(F.FILM_NAME) LIKE '%'||lower(p.query)||'%' ";
    private static final String SEARCH_FILMS_DIR = " EXISTS (SELECT 1 " +
            " FROM FILM_DIRECTORS FD" +
            " JOIN DIRECTORS D ON (FD.DIRECTOR_ID = D.DIRECTOR_ID) " +
            " WHERE FD.FILM_ID = F.FILM_ID " +
            " AND lower(D.DIRECTOR_NAME ) LIKE '%'||lower(p.query)||'%') ";
    private static final String FIND_FILM_IDS_BY_GENRE =
            "SELECT FILM_ID FROM FILM_GENRE WHERE GENRE_ID = ?";

    private static final String FIND_FILM_IDS_BY_YEAR =
            "SELECT FILM_ID FROM FILMS WHERE EXTRACT(YEAR FROM RELEASE_DATE) = ?";

    private static final String FIND_FILM_IDS_BY_GENRE_AND_YEAR =
            "SELECT f.FILM_ID FROM FILMS f " +
                    "JOIN FILM_GENRE fg ON f.FILM_ID = fg.FILM_ID " +
                    "WHERE fg.GENRE_ID = ? AND EXTRACT(YEAR FROM f.RELEASE_DATE) = ?";

    @Override
    public Collection<Film> findAll() {
        Collection<Film> films = jdbc.query(FIND_ALL_QUERY, mapper);
        for (Film film : films) {
            addAdditionalFields(film);
            addLikes(film);
        }
        return films;
    }

    @Override
    public Film create(Film film) {
        addAdditionalFields(film);

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

        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                jdbc.update(INSERT_FILM_DIRECTORS, film.getId(), director.getId());
            }
        }

        return film;
    }

    private void addAdditionalFields(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != 0) {
            film.setMpa(directoryStorage.findMpaById(film.getMpa().getId()));
        }

        //заполняем ID сущностей при обновлении из БД
        if (film.getId() != null) {
            Set<Long> genreIds = findGenresByFilmId(film.getId());
            if (genreIds != null) {
                Set<Genre> genres = new HashSet<>();
                for (Long genreId : genreIds) {
                    genres.add(Genre.builder().id(genreId).build());
                }
                film.setGenres(genres);
            }
            Set<Long> directorIds = findDirectorsByFilmId(film.getId());
            if (directorIds != null) {
                Set<Director> directors = new HashSet<>();
                for (Long directorId : directorIds) {
                    directors.add(Director.builder().id(directorId).build());
                }
                film.setDirectors(directors);
            }
        }

        //Обогащаем сущности наименованиями и при создании и приобновлении
        if (film.getGenres() != null) {
            TreeSet<Genre> genres = new TreeSet<>(Comparator.comparingLong(Genre::getId));
            for (Genre genre : film.getGenres()) {
                genres.add(directoryStorage.findGenreById(genre.getId()));
            }
            film.setGenres(genres);
        }
        if (film.getDirectors() != null) {
            TreeSet<Director> directors = new TreeSet<>(Comparator.comparingLong(Director::getId));
            for (Director director : film.getDirectors()) {
                directors.add(directorStorage.findDirectorById(director.getId()));
            }
            film.setDirectors(directors);
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
        Set<Director> newDirectors = newFilm.getDirectors();
        addAdditionalFields(newFilm);

        /**
         * проверяем были ли изменения режиссеров между тем что прилетело в запросе и что есть в БД
         * если да, то обновляем их
         **/
        if (newDirectors != null && !newFilm.getDirectors().equals(newDirectors)) {
            jdbc.update(DELETE_FILM_DIRECTORS, newFilm.getId());
            for (Director director : newDirectors) {
                jdbc.update(INSERT_FILM_DIRECTORS, newFilm.getId(), director.getId());
            }
            //обновим поля
            addAdditionalFields(newFilm);
        }
        return newFilm;
    }

    @Override
    public Film findFilmById(long filmId) {
        try {
            Film film = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, filmId);
            addAdditionalFields(film);
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
    feedDbStorage.createFeed(userId, filmId, EventType.LIKE, Operation.ADD);
}

    @Override
    public void removeLike(long filmId, long userId) {
        Film film = findFilmById(filmId);
        User user = userDbStorage.findUserById(userId);

        jdbc.update(REMOVE_LIKE, film.getId(), user.getId());
        feedDbStorage.createFeed(userId, filmId, EventType.LIKE, Operation.REMOVE);
    }

    @Override
    public void deleteFilm(long id) {
        int affectedRows = jdbc.update(DELETE_FILM, id);
        if (affectedRows == 0) {
            log.error("Попытка получить несуществующий фильм");
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
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

    private Set<Long> findDirectorsByFilmId(Long filmId) {
        try {
            return new HashSet<>(jdbc.query(FIND_FILM_DIRECTORS_QUERY,
                    new Object[]{filmId},
                    filmDirectorsRowMapper));
        } catch (EmptyResultDataAccessException ignored) {
            log.info("У фильма с id = {} нет режиссеров", filmId);
            return null;
        }
    }

    @Override
    public Collection<Film> findFilmsByDirectorId(long directorId, String sortBy) {
        String sql = FIND_FILMS_BY_DIR;
        if (sortBy.contains("year")) {
            sql += FIND_FILMS_BY_ORDER_BY_YEARS;
        } else if (sortBy.contains("likes")) {
            sql += FIND_FILMS_BY_ORDER_BY_LIKES;
        }

        Collection<Film> films = new ArrayList<>(jdbc.query(sql, new Object[]{directorId}, mapper));
        for (Film film : films) {
            addAdditionalFields(film);
            addLikes(film);
        }
        return films;
    }

    public Collection<Film> getLikedFilms(long userId) {
        // 1. Получаем ID лайкнутых фильмов
        List<Long> filmIds = jdbc.queryForList(FIND_LIKED_FILMS_IDS, Long.class, userId);
        // 2. Для каждого ID получаем полные данные о фильме
        Collection<Film> likedFilms = new HashSet<>();
        for (Long filmId : filmIds) {
            try {
                Film film = findFilmById(filmId); // Используем уже готовый метод
                likedFilms.add(film);
            } catch (NotFoundException e) {
                log.warn("Фильм с ID {} был лайкнут, но не найден в БД", filmId);
            }
        }
        return likedFilms;
    }

    public Collection<Film> searchFilmsByQuery(String query, String by) {
        String sql = SEARCH_FILMS_BY;
        if (by.equals("title")) {
            sql += SEARCH_FILMS_TITLE;
        } else if (by.equals("director")) {
            sql += " AND " + SEARCH_FILMS_DIR;
        } else if (by.contains("director") && by.contains("title")) {
            sql += SEARCH_FILMS_TITLE + " OR " + SEARCH_FILMS_DIR;
        } else {
            throw new InternalServerException("Неверные параметры поиска. Допускается: director, title");
        }
        sql += FIND_FILMS_BY_ORDER_BY_LIKES;
        Collection<Film> films = new ArrayList<>(jdbc.query(sql, new String[]{query}, mapper));
        for (Film film : films) {
            addAdditionalFields(film);
            addLikes(film);
        }
        return films;
    }

    private Collection<Film> findFilmsByYear(int year) {
        List<Long> filmIds = jdbc.queryForList(
                FIND_FILM_IDS_BY_YEAR,
                Long.class, year);
        return convertIdsToFilms(filmIds);
    }

   private Collection<Film> findFilmsByGenre(long genreId) {
        List<Long> filmIds = jdbc.queryForList(
                FIND_FILM_IDS_BY_GENRE,
                Long.class, genreId);
        return convertIdsToFilms(filmIds);
    }


    private Collection<Film> findFilmsByGenreAndYear(long genreId, Integer year) {

        List<Long> filmIds = jdbc.queryForList(
                FIND_FILM_IDS_BY_GENRE_AND_YEAR,
                Long.class, genreId, year);
        return convertIdsToFilms(filmIds);
    }

    public Collection<Film> findFilmsWithFilters(Long genreId, Integer year) {
        if (genreId != null && year != null) {
            return findFilmsByGenreAndYear(genreId, year);
        } else if (genreId != null) {
            return findFilmsByGenre(genreId);
        } else if (year != null) {
            return findFilmsByYear(year);
        }
        return findAll();
    }

    private Collection<Film> convertIdsToFilms(List<Long> filmIds) {
        return filmIds.stream()
                .map(this::findFilmById)
                .filter(Objects::nonNull)
                .peek(film -> {
                addAdditionalFields(film);
                addLikes(film);
                })
                .collect(Collectors.toList());
    }

}

