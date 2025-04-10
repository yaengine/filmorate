package ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    public Collection<Film> findAll();

    public Film create(Film film);

    public Film update(Film newFilm);

    public Film findFilmById(long id);

    public void addLike(long filmId, long userId);

    public void removeLike(long filmId, long userId);

    public Collection<Film> findFilmsByDirectorId(long directorId, String sortBy);

    public void deleteFilm(long id);

    public Collection<Film> searchFilmsByQuery(String query, String by);
}