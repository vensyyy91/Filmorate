package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserRepository implements Repository<User> {
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
    public void save(int id, User user) {
        users.put(id, user);
    }
}