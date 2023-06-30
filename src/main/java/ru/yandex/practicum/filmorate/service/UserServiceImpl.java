package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.Repository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private final Repository<User> repository;
    private int id;

    @Override
    public List<User> getAllUsers() {
        log.info("Возвращен список пользователей: " + repository.getAll().toString());
        return repository.getAll();
    }

    @Override
    public User addUser(User user) {
        int id = generateID();
        user.setId(id);
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        repository.save(id, user);
        log.info(String.format("Добавлен пользователь: id=%d, email=%s", id, user.getEmail()));
        return user;
    }

    @Override
    public User updateUser(User user) {
        int id = user.getId();
        if (repository.getAll().stream().noneMatch(u -> u.getId() == id)) {
            throw new ValidationException("Пользователя с таким id не существует.");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        repository.save(id, user);
        log.info(String.format("Обновлен пользователь: id=%d, email=%s", id, user.getEmail()));
        return user;
    }

    private int generateID() {
        return ++id;
    }
}