package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.Repository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmServiceImpl implements FilmService {
    @Autowired
    private final Repository<Film> repository;
    private int id;

    public List<Film> getAllFilms() {
        log.info("Возвращен список пользователей: " + repository.getAll().toString());
        return repository.getAll();
    }

    public Film addFilm(Film film) {
        int id = generateID();
        film.setId(id);
        repository.save(id, film);
        log.info(String.format("Добавлен фильм: id=%d, name=%s", id, film.getName()));
        return film;
    }

    public Film updateFilm(Film film) {
        int id = film.getId();
        if (repository.getAll().stream().noneMatch(f -> f.getId() == id)) {
            throw new ValidationException("Фильма с таким id не существует.");
        }
        repository.save(id, film);
        log.info(String.format("Обновлен фильм: id=%d, name=%s", id, film.getName()));
        return film;
    }

    private int generateID() {
        return ++id;
    }
}
