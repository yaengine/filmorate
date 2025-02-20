package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private static final String EMPTY_NAME_ERR = "Название не может быть пустым";
    private static final String LONG_DESC_ERR = "Максимальная длина описания — 200 символов";
    private static final String REL_DATE_ERR = "Дата релиза — не раньше 28 декабря 1895 года";
    private static final String DURATION_ERR = "Продолжительность фильма должна быть положительным числом";
    private static final String ID_ERR = "Id должен быть указан";



    @GetMapping
    public Collection<Film> findAll() {
        log.trace("Начинаем возвращать все фильмы");
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
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
                .build();

        // сохраняем новую публикацию в памяти приложения
        films.put(flm.getId(), flm);

        log.trace("Успешно создали фильм");
        log.trace(flm.toString());
        return flm;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
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

            log.trace("Успешно обновили фильм");
            log.trace(oldFilm.toString());
            return oldFilm;
        } else {
            log.error("Пользователь с id = {} не найден", newFilm.getId());
            throw new ValidationException("Пользователь с id = " + newFilm.getId() + " не найден");
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
