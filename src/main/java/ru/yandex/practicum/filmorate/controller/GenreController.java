package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@Slf4j
public class GenreController {
    private final FilmService service;

    @Autowired
    public GenreController(FilmService service) {
        this.service = service;
    }

    @GetMapping
    public List<Genre> getAll() {
        log.info("Получен запрос GET /genres");
        return service.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre get(@PathVariable int id) {
        log.info("Получен запрос GET /genres/" + id);
        return service.getGenreById(id);
    }
}