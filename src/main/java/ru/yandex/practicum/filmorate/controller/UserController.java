package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping
    public List<User> getAll() {
        log.info("Получен запрос GET /users");
        return service.getAllUsers();
    }

    @GetMapping("/{id}")
    public User get(@PathVariable int id) {
        log.info("Получен запрос GET /users/" + id);
        return service.getUser(id);
    }

    @PostMapping
    public User add(@Valid @RequestBody User user) {
        log.info("Получен запрос POST /users");
        return service.addUser(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Получен запрос PUT /users");
        return service.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Получен запрос PUT /users/{}/friends/{}", id, friendId);
        service.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Получен запрос DELETE /users/{}/friends/{}", id, friendId);
        service.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getAllFriends(@PathVariable int id) {
        log.info("Получен запрос GET /users/{}/friends", id);
        return service.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        log.info("Получен запрос GET /users/{}/friends/common/{}", id, otherId);
        return service.getCommonFriends(id, otherId);
    }
}