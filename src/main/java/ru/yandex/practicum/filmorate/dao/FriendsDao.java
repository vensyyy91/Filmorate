package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendsDao {
    void save(int userId, int friendId);

    void delete(int userId, int friendId);

    List<User> getAllById(int userId);

    List<User> getCommonById(int userId, int otherId);
}