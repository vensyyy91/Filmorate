package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FriendsDao;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
        return jdbcTemplate.query(sql, this::makeUser, userId);
    }

    @Override
    public List<User> getCommonById(int userId, int otherId) {
        String sql = "SELECT * FROM users AS u, friends AS f, friends AS o " +
                "WHERE u.id = f.friend_id AND u.id = o.friend_id AND f.user_id = ? AND o.user_id = ?";
        return jdbcTemplate.query(sql, this::makeUser, userId, otherId);
    }

    private User makeUser(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("id");
        String email = rs.getString("email");
        String login = rs.getString("login");
        String name = rs.getString("name");
        LocalDate birthday = rs.getDate("birthday").toLocalDate();

        return new User(id, email, login, name, birthday);
    }
}