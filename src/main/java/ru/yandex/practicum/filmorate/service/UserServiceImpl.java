package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserStorage storage;
    private int id;

    @Autowired
    public UserServiceImpl(UserStorage storage) {
        this.storage = storage;
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Возвращен список пользователей: " + storage.getAll().toString());
        return storage.getAll();
    }

    @Override
    public User getUser(int id) {
        log.info("Возвращен пользователь: " + storage.get(id));
        return storage.get(id);
    }

    @Override
    public User addUser(User user) {
        int id = generateID();
        user.setId(id);
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        storage.save(id, user);
        log.info(String.format("Добавлен пользователь: id=%d, email=%s", id, user.getEmail()));
        return user;
    }

    @Override
    public User updateUser(User user) {
        int id = user.getId();
        if (storage.getAll().stream().noneMatch(u -> u.getId() == id)) {
            throw new UserNotFoundException(String.format("Пользователя с id=%d не существует.", id));
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        storage.save(id, user);
        log.info(String.format("Обновлен пользователь: id=%d, email=%s", id, user.getEmail()));
        return user;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        User user = storage.get(userId);
        User friend = storage.get(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info(String.format("Пользователи с id=%d и id=%d добавились в друзья.", userId, friendId));
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        User user = storage.get(userId);
        User friend = storage.get(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info(String.format("Пользователи с id=%d и id=%d удалились из друзей.", userId, friendId));
    }

    @Override
    public List<User> getAllFriends(int userId) {
        List<User> userFriends = new ArrayList<>();
        Set<Integer> userFriendsId = storage.get(userId).getFriends();
        if (!userFriendsId.isEmpty()) {
            userFriends = userFriendsId.stream().map(storage::get).collect(Collectors.toList());
        }
        log.info(String.format("Возвращен список всех друзей пользователя с id=%d: %s", userId, userFriends));
        return userFriends;
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        List<User> commonFriends = new ArrayList<>();
        Set<Integer> userFriendsId = storage.get(userId).getFriends();
        Set<Integer> otherFriendsId = storage.get(otherId).getFriends();
        if (!userFriendsId.isEmpty() && !otherFriendsId.isEmpty()) {
            commonFriends = userFriendsId.stream()
                    .filter(otherFriendsId::contains)
                    .map(storage::get)
                    .collect(Collectors.toList());
        }
        log.info(String.format("Возвращен список всех общих друзей у пользователей с id=%d и id=%d: %s",
                userId, otherId, commonFriends));
        return commonFriends;
    }

    private int generateID() {
        return ++id;
    }
}