package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.dao.FriendsDao;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final FriendsDao friendsDao;
    private final FilmDao filmDao;
    private final EventDao eventDao;

    @Override
    public List<User> getAllUsers() {
        List<User> users = userDao.getAll();
        log.info("Возвращен список пользователей: " + users.toString());

        return users;
    }

    @Override
    public User getUser(int id) {
        User user = userDao.getById(id);
        log.info("Возвращен пользователь: " + user);

        return user;
    }

    @Override
    public User addUser(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        User newUser = userDao.save(user);
        log.info(String.format("Добавлен пользователь: id=%d, email=%s", newUser.getId(), newUser.getEmail()));

        return newUser;
    }

    @Override
    public User updateUser(User user) {
        checkIfUserExists(user.getId());
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        userDao.save(user);
        log.info(String.format("Обновлен пользователь: id=%d, email=%s", user.getId(), user.getEmail()));

        return user;
    }

    @Override
    public void deleteUser(int userId) {
        checkIfUserExists(userId);
        userDao.delete(userId);
        log.info("Удален пользователь с id={}", userId);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        checkIfUserExists(userId);
        checkIfUserExists(friendId);
        friendsDao.save(userId, friendId);
        eventDao.writeEvent(userId, EventType.FRIEND, Operation.ADD, friendId);
        log.info(String.format("Пользователь с id=%d добавил в друзья пользователя с id=%d.", userId, friendId));
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        checkIfUserExists(userId);
        checkIfUserExists(friendId);
        friendsDao.delete(userId, friendId);
        eventDao.writeEvent(userId, EventType.FRIEND, Operation.REMOVE, friendId);
        log.info(String.format("Пользователь с id=%d удалил из друзей пользователя с id=%d.", userId, friendId));
    }

    @Override
    public List<User> getAllFriends(int userId) {
        checkIfUserExists(userId);
        List<User> userFriends = friendsDao.getAllById(userId);
        log.info(String.format("Возвращен список всех друзей пользователя с id=%d: %s", userId, userFriends));

        return userFriends;
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        checkIfUserExists(userId);
        checkIfUserExists(otherId);
        List<User> commonFriends = friendsDao.getCommonById(userId, otherId);
        log.info(String.format("Возвращен список всех общих друзей у пользователей с id=%d и id=%d: %s",
                userId, otherId, commonFriends));

        return commonFriends;
    }

    @Override
    public List<Film> getRecommendations(int id) {
        checkIfUserExists(id);
        List<Film> films = filmDao.getRecommendations(id);
        log.info("Возвращен список рекомендованных фильмов для пользователя с id={}: {}", id, films);

        return films;
    }

    @Override
    public List<Event> getUserFeed(int id) {
        checkIfUserExists(id);

        return eventDao.getUserEvents(id);
    }

    private void checkIfUserExists(int id) {
        userDao.getById(id);
    }
}