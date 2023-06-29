package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserService service;

    @GetMapping
    public List<User> getAll() {
        return service.getAllUsers();
    }

    @PostMapping
    public User add(@Valid @RequestBody User user) {
        return service.addUser(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return service.updateUser(user);
    }
}