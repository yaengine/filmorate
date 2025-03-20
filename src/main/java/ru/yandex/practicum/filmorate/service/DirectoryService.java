package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.directory.DirectoryStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectoryService {
    private final DirectoryStorage drectoryStorage;

    public Collection<Genre> findAllGenres() {
        return drectoryStorage.findAllGenres();
    }

    public Genre findGenreById(long genreId) {
        return drectoryStorage.findGenreById(genreId);
    }

    public Collection<Mpa> findAllMpa() {
        return drectoryStorage.findAllMpa();
    }

    public Mpa findMpaById(long mpaId) {
        return drectoryStorage.findMpaById(mpaId);
    }
}
