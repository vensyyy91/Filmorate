package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@AllArgsConstructor
public class UserController {
    @Autowired
    private final UserService service;

    @GetMapping
    public List<User> getAll() {
        log.info("Получен запрос GET /users");
        return service.getAllUsers();
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
}