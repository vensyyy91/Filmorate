package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;

public interface UserStorage {
    Map<Integer, User> getMap();

    List<User> getAll();

    User get(int id);

    void save(int id, User user);
}
