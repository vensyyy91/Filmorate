package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Slf4j
public class MpaController {
    private final FilmService service;

    @Autowired
    public MpaController(FilmService service) {
        this.service = service;
    }

    @GetMapping
    public List<Mpa> getAll() {
        log.info("Получен запрос GET /mpa");
        return service.getAllMpa();
    }

    @GetMapping("/{id}")
    public Mpa get(@PathVariable int id) {
        log.info("Получен запрос GET /mpa/" + id);
        return service.getMpaById(id);
    }
}