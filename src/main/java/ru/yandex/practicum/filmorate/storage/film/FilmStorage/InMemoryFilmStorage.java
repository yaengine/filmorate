package ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private static final String EMPTY_NAME_ERR = "Название не может быть пустым";
    private static final String LONG_DESC_ERR = "Максимальная длина описания — 200 символов";
    private static final String REL_DATE_ERR = "Дата релиза — не раньше 28 декабря 1895 года";
    private static final String DURATION_ERR = "Продолжительность фильма должна быть положительным числом";
    private static final String ID_ERR = "Id должен быть указан";

    public Collection<Film> findAll() {
        log.trace("Начинаем возвращать все фильмы");
        return films.values();
    }

    @Override
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
        log.trace("Проверки пройдены");

        // формируем дополнительные данные
        Film flm = Film.builder()
                .id(getNextId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .likes(new HashSet<>())
                .build();

        // сохраняем новую публикацию в памяти приложения
        films.put(flm.getId(), flm);

        log.trace("Успешно создали фильм");
        log.trace(flm.toString());
        return flm;
    }

    @Override
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

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            if (newFilm.getLikes() != null) {
                oldFilm.setLikes(newFilm.getLikes());
            }

            log.trace("Успешно обновили фильм");
            log.trace(oldFilm.toString());
            return oldFilm;
        } else {
            log.error("Фильм с id = {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }
    }

    @Override
    public Film findFilmById(long id) {
        if (films.containsKey(id)) {
            return films.get(id);
        } else {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
