package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Validated
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

    @GetMapping(value = "/director/{directorId}")
    public Collection<Film> findFilmsByDirectorId(@PathVariable long directorId,
                                                  @RequestParam(required = false)
                                                  @Pattern(regexp = "year|likes",
                                                          message = "Неверные параметры сортировки. Допускается: year, likes")
                                                  String sortBy) {
        return filmService.findFilmsByDirectorId(directorId, sortBy);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable long filmId) {
        filmService.deleteFilm(filmId);
    }

    @GetMapping("/common")
    public Collection<Film> findCommonFilms(@RequestParam long userId,
                                            @RequestParam long friendId) {
        return filmService.findCommonFilms(userId, friendId);
    }

    @GetMapping("/search")
    public Collection<Film> searchFilmsByQuery(
            @RequestParam String query,
            @RequestParam @Valid
            @Pattern(regexp = "^(director|title|director,title|title,director)$",
                    message = "Неверные параметры поиска. Допускается: director, title")
            String by) {
        return filmService.searchFilmsByQuery(query, by);
    }
}
