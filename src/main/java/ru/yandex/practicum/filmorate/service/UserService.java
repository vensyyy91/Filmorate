package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
@Setter
public class UserService {
    @Autowired
    private final Repository<User> repository;
    private int id;

    public List<User> getAllUsers() {
        return repository.getAll();
    }

    public User addUser(User user) {
        int id = generateID();
        user.setId(id);
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        repository.save(id, user);
        return user;
    }

    public User updateUser(User user) {
        int id = user.getId();
        if (repository.getAll().stream().noneMatch(u -> u.getId() == id)) {
            throw new ValidationException("Пользователя с таким id не существует.");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        repository.save(id, user);
        return user;
    }

    private int generateID() {
        return ++id;
    }
}
