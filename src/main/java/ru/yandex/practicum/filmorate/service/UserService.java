package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User getUser(int id);

    User addUser(User user);

    User updateUser(User user);

    void addFriend(int userId, int friendId);

    void deleteFriend(int userId, int friendId);

    List<User> getAllFriends(int userId);

    List<User> getCommonFriends(int userId, int otherId);
}