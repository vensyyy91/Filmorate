package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
@Slf4j
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Director> getAll() {
        log.info("Получен запрос GET /directors");
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director get(@PathVariable int id) {
        log.info("Получен запрос GET /directors/{}", id);
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director add(@Valid @RequestBody Director director) {
        log.info("Получен запрос POST /directors");
        return directorService.addDirector(director);
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        log.info("Получен запрос PUT /directors");
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        log.info("Получен запрос DELETE /directors/{}", id);
        directorService.deleteDirector(id);
    }
}