package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserDao {
    List<User> getAll();

    User getById(int id);

    User save(User user);

    void delete(int userId);
}
