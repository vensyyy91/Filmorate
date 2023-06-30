package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@AllArgsConstructor
public class FilmController {
    @Autowired
    private final FilmService service;

    @GetMapping
    public List<Film> getAll() {
        log.info("Получен запрос GET /films");
        return service.getAllFilms();
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        log.info("Получен запрос POST /films");
        return service.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос PUT /films");
        return service.updateFilm(film);
    }
}
