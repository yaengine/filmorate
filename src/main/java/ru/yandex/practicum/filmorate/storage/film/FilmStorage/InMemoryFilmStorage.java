package ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.UserStorage.InMemoryUserStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
@Component
@Qualifier("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private InMemoryUserStorage inMemoryUserStorage;

    public Collection<Film> findAll() {
        log.trace("Начинаем возвращать все фильмы");
        return films.values();
    }

    @Override
    public Film create(Film film) {
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
    }

    @Override
    public Film findFilmById(long id) {
        if (films.containsKey(id)) {
            return films.get(id);
        } else {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
    }

    @Override
    public void addLike(long filmId, long userId) {
        findFilmById(filmId).getLikes().add(inMemoryUserStorage.findUserById(userId).getId());
    }

    @Override
    public void removeLike(long filmId, long userId) {
        findFilmById(filmId).getLikes().remove(inMemoryUserStorage.findUserById(userId).getId());
    }

    @Override
    public void deleteFilm(long id) {

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
