package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Set;

public interface DirectorDao {
    List<Director> getAll();

    Director getById(int id);

    Director save(Director director);

    Director update(Director director);

    void delete(int id);

    Set<Director> getAllByFilmId(int filmId);
}