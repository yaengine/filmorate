package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    @Autowired
    private final FilmStorage filmStorage;
    @Autowired
    private final UserStorage userStorage;

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public void addLike(long filmId, long userId) {
        filmStorage.findFilmById(filmId).getLikes().add(userStorage.findUserById(userId).getId());
    }

    public void removeLike(long filmId, long userId) {
        filmStorage.findFilmById(filmId).getLikes().remove(userStorage.findUserById(userId).getId());
    }

    public Collection<Film> findTopFilms(long count) {
        return filmStorage.findAll().isEmpty() ? new ArrayList<>() :
                filmStorage.findAll()
                        .stream()
                        .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                        .limit(count)
                        .collect(Collectors.toList());
    }
}
