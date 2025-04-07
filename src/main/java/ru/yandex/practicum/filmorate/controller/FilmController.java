package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    @Autowired
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/{filmId}")
    public Film findFilmById(@PathVariable long filmId) {
        return filmService.findFilmById(filmId);
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
     return filmService.update(newFilm);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable long filmId, @PathVariable long userId) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(@PathVariable long filmId, @PathVariable long userId) {
        filmService.removeLike(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> findTopFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.findTopFilms(count);
    }

    @GetMapping(value = "/director/{directorId}", params = "sortBy")
    public Collection<Film> findFilmsByDirectorId(@PathVariable long directorId,
                                                  @RequestParam(required = false)
                                                  List<@Pattern(regexp = "^(year|likes)$",
                                                  message = "Неверные параметры сортировки. Допускается: year, likes")
                                                  String> sortBy) {
        return filmService.findFilmsByDirectorId(directorId, sortBy);
    }
}
