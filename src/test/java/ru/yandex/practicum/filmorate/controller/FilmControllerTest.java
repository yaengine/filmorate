package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {

    FilmController filmController = new FilmController();
    Film film;

    @BeforeEach
    void beforeEach() {
         film = Film.builder()
                .id(1L)
                .name("FilmName")
                .description("FilmDescr")
                .releaseDate(LocalDate.parse("2001-10-05"))
                .duration(100)
                .build();
    }

    @Test
    void create() {
        Film flm = filmController.create(film);
        assertEquals(film, flm, "Фильм не создался");
    }

    @Test
    void createWithEmptyName() {
        film.setName("");
        assertThrows(ValidationException.class, () -> filmController.create(film), "Создание фильма с пустым " +
                "полем name должно приводить к исключению");
    }

    @Test
    void createWithLongDescription() {
        film.setDescription(new String(new char[201]));
        assertThrows(ValidationException.class, () -> filmController.create(film), "Создание фильма с полем " +
                "Description размером > 200 должно приводить к исключению");
    }

    @Test
    void createWithOldReleaseDate() {
        film.setReleaseDate(LocalDate.parse("1895-12-27"));
        assertThrows(ValidationException.class, () -> filmController.create(film), "Создание фильма с полем " +
                "ReleaseDate младше 1895-12-28 должно приводить к исключению");
    }

    @Test
    void createWithWrongDuration() {
        film.setDuration(-1);
        assertThrows(ValidationException.class, () -> filmController.create(film), "Создание фильма с полем " +
                "Duration < 0 должно приводить к исключению");
    }

    @Test
    void update() {
        Film flm = filmController.create(film);
        Film flm1 = filmController.update(flm);
        assertEquals(flm, flm1, "Фильм не обновился");
    }

    @Test
    void updateWithEmptyName() {
        Film flm = filmController.create(film);
        flm.setName("");
        assertThrows(ValidationException.class, () -> filmController.update(flm), "Обновление фильма с пустым " +
                "полем name должно приводить к исключению");
    }

    @Test
    void updateWithLongDescription() {
        Film flm = filmController.create(film);
        flm.setDescription(new String(new char[201]));
        assertThrows(ValidationException.class, () -> filmController.update(flm), "Обновление фильма с полем " +
                "Description размером > 200 должно приводить к исключению");
    }

    @Test
    void updateWithOldReleaseDate() {
        Film flm = filmController.create(film);
        flm.setReleaseDate(LocalDate.parse("1895-12-27"));
        assertThrows(ValidationException.class, () -> filmController.update(flm), "Обновление фильма с полем " +
                "ReleaseDate младше 1895-12-28 должно приводить к исключению");
    }

    @Test
    void updateWithWrongDuration() {
        Film flm = filmController.create(film);
        flm.setDuration(-1);
        assertThrows(ValidationException.class, () -> filmController.update(flm), "Обновление фильма с полем " +
                "Duration < 0 должно приводить к исключению");
    }

    @Test
    void findAll() {
        Film flm = filmController.create(film);
        assertTrue(filmController.findAll().contains(flm), "Фильм не найден");
    }
}
