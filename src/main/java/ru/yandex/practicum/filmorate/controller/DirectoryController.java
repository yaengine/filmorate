package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.DirectoryService;

import java.util.Collection;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class DirectoryController {
    @Autowired
    private final DirectoryService directoryService;

    @GetMapping("/genres")
    public Collection<Genre> findAllGenres() {
        return directoryService.findAllGenres();
    }

    @GetMapping("/genres/{id}")
    public Genre findGenreById(@PathVariable long id) {
        return directoryService.findGenreById(id);
    }

    @GetMapping("/mpa")
    public Collection<Mpa> findAllMpa() {
        return directoryService.findAllMpa();
    }

    @GetMapping("/mpa/{id}")
    public Mpa findMpaById(@PathVariable long id) {
        return directoryService.findMpaById(id);
    }
}
