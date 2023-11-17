package ru.yandex.practicum.filmorate.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FriendsDao;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final FriendsDao friendsDao;

    @Autowired
    public UserServiceImpl(@Qualifier("UserDbStorage") UserDao userDao, FriendsDao friendsDao) {
        this.userDao = userDao;
        this.friendsDao = friendsDao;
    }

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
        userDao.getById(user.getId()); // проверка наличия пользователя
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        userDao.save(user);
        log.info(String.format("Обновлен пользователь: id=%d, email=%s", user.getId(), user.getEmail()));

        return user;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        userDao.getById(userId); // проверка наличия пользователя
        userDao.getById(friendId); // проверка наличия пользователя
        friendsDao.save(userId, friendId);
        log.info(String.format("Пользователь с id=%d добавил в друзья пользователя с id=%d.", userId, friendId));
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        userDao.getById(userId); // проверка наличия пользователя
        userDao.getById(friendId); // проверка наличия пользователя
        friendsDao.delete(userId, friendId);
        log.info(String.format("Пользователь с id=%d удалил из друзей пользователя с id=%d.", userId, friendId));
    }

    @Override
    public List<User> getAllFriends(int userId) {
        userDao.getById(userId); // проверка наличия пользователя
        List<User> userFriends = friendsDao.getAllById(userId);
        log.info(String.format("Возвращен список всех друзей пользователя с id=%d: %s", userId, userFriends));
        return userFriends;
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        userDao.getById(userId); // проверка наличия пользователя
        userDao.getById(otherId); // проверка наличия пользователя
        List<User> commonFriends = friendsDao.getCommonById(userId, otherId);
        log.info(String.format("Возвращен список всех общих друзей у пользователей с id=%d и id=%d: %s",
                userId, otherId, commonFriends));
        return commonFriends;
    }
}