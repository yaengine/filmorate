package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.directory.DirectoryStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private static final String EMPTY_NAME_ERR = "Название не может быть пустым";
    private static final String LONG_DESC_ERR = "Максимальная длина описания — 200 символов";
    private static final String REL_DATE_ERR = "Дата релиза — не раньше 28 декабря 1895 года";
    private static final String DURATION_ERR = "Продолжительность фильма должна быть положительным числом";
    private static final String ID_ERR = "Id должен быть указан";

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectoryStorage directoryStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage, DirectoryStorage directoryStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.directoryStorage = directoryStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findFilmById(Long filmId) {
        return filmStorage.findFilmById(filmId);
    }

    public Film create(Film film) {
        // проверяем выполнение необходимых условий
        log.trace("Начинаем создавать фильм");
        log.trace(film.toString());

        if (film.getName() == null || film.getName().isBlank()) {
            log.error(EMPTY_NAME_ERR);
            throw new ValidationException(EMPTY_NAME_ERR);
        }
        if (film.getDescription().length() > 200) {
            log.error(LONG_DESC_ERR);
            throw new ValidationException(LONG_DESC_ERR);
        }
        if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28"))) {
            log.error(REL_DATE_ERR);
            throw new ValidationException(REL_DATE_ERR);
        }
        if (film.getDuration() == null || film.getDuration() < 0) {
            log.error(DURATION_ERR);
            throw new ValidationException(DURATION_ERR);
        }

        if (film.getName() == null || film.getName().isBlank()) {
            log.error(EMPTY_NAME_ERR);
            throw new ValidationException(EMPTY_NAME_ERR);
        }

        log.trace("Проверки пройдены");

        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        // проверяем необходимые условия
        log.trace("Начинаем редактировать фильм");
        log.trace(newFilm.toString());

        if (newFilm.getId() == null) {
            log.error(ID_ERR);
            throw new ValidationException(ID_ERR);
        }
        if (newFilm.getName() == null || newFilm.getName().isBlank()) {
            log.error(EMPTY_NAME_ERR);
            throw new ValidationException(EMPTY_NAME_ERR);
        }
        if (newFilm.getDescription().length() > 200) {
            log.error(LONG_DESC_ERR);
            throw new ValidationException(LONG_DESC_ERR);
        }
        if (newFilm.getReleaseDate().isBefore(LocalDate.parse("1895-12-28"))) {
            log.error(REL_DATE_ERR);
            throw new ValidationException(REL_DATE_ERR);
        }
        if (newFilm.getDuration() == null || newFilm.getDuration() < 0) {
            log.error(DURATION_ERR);
            throw new ValidationException(DURATION_ERR);
        }
        log.trace("Проверки пройдены");

        if (findAll().stream().collect(Collectors.toMap(Film::getId, f -> f)).containsKey(newFilm.getId())) {
          return filmStorage.update(newFilm);
        } else {
            log.error("Фильм с id = {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }
    }

    public void addLike(long filmId, long userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> findTopFilms(long count) {
        Collection<Film> allFilms = filmStorage.findAll();
        return allFilms.stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public void deleteFilm(long id) {
        filmStorage.deleteFilm(id);
    }

    public Collection<Film> findFilmsByDirectorId(long directorId, String sortBy) {
        return filmStorage.findFilmsByDirectorId(directorId, sortBy);
    }

public List<Film> findCommonFilms(long userId, long friendId) {
    Collection<Film> userFilms;
    Collection<Film> friendFilms;
    try {
        userFilms = filmStorage.getLikedFilms(userId);
        friendFilms = filmStorage.getLikedFilms(friendId);
    } catch (EmptyResultDataAccessException e) {
        throw new NotFoundException("Один из пользователей не найден");
    }

    List<Film> commonFilms = userFilms.stream()
            .filter(friendFilms::contains)
            .collect(Collectors.toList());

    commonFilms.sort((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()));
    return commonFilms;
}

    public Collection<Film> findFilmsByYear(int year) {
        return sortByLikes(filmStorage.findFilmsByYear(year));
    }

    public Collection<Film> findFilmsByGenre(long genreId) {
        return sortByLikes(filmStorage.findFilmsByGenre(genreId));
    }

    public Collection<Film> findFilmsByYearAndGenre(Integer year, long genreId) {
        return sortByLikes(filmStorage.findFilmsByGenreAndYear(genreId, year));
    }

    private Collection<Film> sortByLikes(Collection<Film> films) {
        return films.stream()
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                .collect(Collectors.toList());
    }

public Collection<Film> searchFilmsByQuery(String query, String by) {
    return filmStorage.searchFilmsByQuery(query, by);
    }
}
