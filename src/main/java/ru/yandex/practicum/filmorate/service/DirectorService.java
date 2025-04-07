package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage drectorStorage;

    public Collection<Director> findAllDirectors() {
        return drectorStorage.findAllDirectors();
    }

    public Director findDirectorById(long directorId) {
        return drectorStorage.findDirectorById(directorId);
    }

    public Director create(Director director) {
        return drectorStorage.create(director);
    }

    public Director update(Director director) {
        if (findDirectorById(director.getId()) != null) {
            return drectorStorage.update(director);
        } else {
            log.error("Режисер с id = {} не найден", director.getId());
            throw new NotFoundException("Режисер с id = " + director.getId() + " не найден");
        }
    }

    public void delete(long directorId) {
        if (findDirectorById(directorId) != null) {
            drectorStorage.delete(directorId);
        } else {
            log.error("Режисер с id = {} не найден", directorId);
            throw new NotFoundException("Режисер с id = " + directorId + " не найден");
        }
    }

}
