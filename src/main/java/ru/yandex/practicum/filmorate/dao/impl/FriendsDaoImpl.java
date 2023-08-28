package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FriendsDao;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.util.Mapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FriendsDaoImpl implements FriendsDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(int userId, int friendId) {
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void delete(int userId, int friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getAllById(int userId) {
        String sql = "SELECT * FROM users AS u, friends AS f WHERE u.id = f.friend_id AND f.user_id = ?";
        return jdbcTemplate.query(sql, Mapper::makeUser, userId);
    }

    @Override
    public List<User> getCommonById(int userId, int otherId) {
        String sql = "SELECT * FROM users AS u, friends AS f, friends AS o " +
                "WHERE u.id = f.friend_id AND u.id = o.friend_id AND f.user_id = ? AND o.user_id = ?";
        return jdbcTemplate.query(sql, Mapper::makeUser, userId, otherId);
    }
}