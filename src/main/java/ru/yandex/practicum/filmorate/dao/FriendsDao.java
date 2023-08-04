package ru.yandex.practicum.filmorate.dao;

import java.util.List;

public interface FriendsDao {
    void save(int userId, int friendId);

    void delete(int userId, int friendId);

    List<Integer> getAllById(int userId);

    List<Integer> getCommonById(int userId, int otherId);
}