package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    @Autowired
    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> findAllDirectors() {
        return directorService.findAllDirectors();
    }

    @GetMapping("/{id}")
    public Director findDirectorById(@PathVariable long id) {
        return directorService.findDirectorById(id);
    }

    @PostMapping
    public Director create(@RequestBody Director director) {
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@RequestBody Director director) {
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        directorService.delete(id);
    }
}
