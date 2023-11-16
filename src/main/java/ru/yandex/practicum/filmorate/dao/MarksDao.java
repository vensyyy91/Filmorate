package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface MarksDao {
    void save(int filmId, int userId, int mark);

    void update(int filmId, int userId, int mark);

    void delete(int filmId, int userId);

    Integer get(int filmId, int userId);

    List<Film> getTop(int count);

    List<Integer> getAllUsersByFilmId(int filmId);

    Double getFilmRate(int filmId);
}