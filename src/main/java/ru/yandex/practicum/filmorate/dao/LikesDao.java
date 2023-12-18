package ru.yandex.practicum.filmorate.dao;

import java.util.List;

public interface LikesDao {
    void save(int id, int userId);

    void delete(int id, int userId);

    List<Integer> getAllByFilmId(int filmId);
}