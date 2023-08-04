package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FriendsDao;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final FriendsDao friendsDao;

    @Autowired
    public UserServiceImpl(@Qualifier("UserDbStorage") UserStorage userStorage, FriendsDao friendsDao) {
        this.userStorage = userStorage;
        this.friendsDao = friendsDao;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userStorage.getAll();
        log.info("Возвращен список пользователей: " + users.toString());

        return users;
    }

    @Override
    public User getUser(int id) {
        User user = userStorage.getById(id);
        log.info("Возвращен пользователь: " + user);

        return user;
    }

    @Override
    public User addUser(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        User newUser = userStorage.save(user);
        log.info(String.format("Добавлен пользователь: id=%d, email=%s", newUser.getId(), newUser.getEmail()));

        return newUser;
    }

    @Override
    public User updateUser(User user) {
        userStorage.getById(user.getId()); // проверка наличия пользователя
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        userStorage.save(user);
        log.info(String.format("Обновлен пользователь: id=%d, email=%s", user.getId(), user.getEmail()));

        return user;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        userStorage.getById(userId); // проверка наличия пользователя
        userStorage.getById(friendId); // проверка наличия пользователя
        friendsDao.save(userId, friendId);
        log.info(String.format("Пользователь с id=%d добавил в друзья пользователя с id=%d.", userId, friendId));
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        userStorage.getById(userId); // проверка наличия пользователя
        userStorage.getById(friendId); // проверка наличия пользователя
        friendsDao.delete(userId, friendId);
        log.info(String.format("Пользователь с id=%d удалил из друзей пользователя с id=%d.", userId, friendId));
    }

    @Override
    public List<User> getAllFriends(int userId) {
        userStorage.getById(userId); // проверка наличия пользователя
        List<User> userFriends = new ArrayList<>();
        List<Integer> userFriendsId = friendsDao.getAllById(userId);
        if (!userFriendsId.isEmpty()) {
            userFriends.addAll(userFriendsId.stream().map(userStorage::getById).collect(Collectors.toList()));
        }
        log.info(String.format("Возвращен список всех друзей пользователя с id=%d: %s", userId, userFriends));
        return userFriends;
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        userStorage.getById(userId); // проверка наличия пользователя
        userStorage.getById(otherId); // проверка наличия пользователя
        List<User> commonFriends = new ArrayList<>();
        List<Integer> commonFriendsId = friendsDao.getCommonById(userId, otherId);
        if (!commonFriendsId.isEmpty()) {
            commonFriends.addAll(commonFriendsId.stream().map(userStorage::getById).collect(Collectors.toList()));
        }
        log.info(String.format("Возвращен список всех общих друзей у пользователей с id=%d и id=%d: %s",
                userId, otherId, commonFriends));
        return commonFriends;
    }
}