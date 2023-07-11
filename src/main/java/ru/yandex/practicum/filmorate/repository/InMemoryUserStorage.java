package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public Map<Integer, User> getMap() {
        return users;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User get(int id) {
        if (users.get(id) == null) {
            throw new UserNotFoundException(String.format("Пользователь с id=%d не найден.", id));
        }
        return users.get(id);
    }

    @Override
    public void save(int id, User user) {
        users.put(id, user);
    }
}