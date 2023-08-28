package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface LikesDao {
    void save(int id, int userId);

    void delete(int id, int userId);

    List<Film> getTop(int count);

    List<Integer> getAllByFilmId(int filmId);
}