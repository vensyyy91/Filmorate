package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface FilmStorage {
    Map<Integer, Film> getMap();

    List<Film> getAll();

    Film get(int id);

    void save(int id, Film film);
}
