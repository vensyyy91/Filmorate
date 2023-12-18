package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.LikesDao;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LikesDaoImpl implements LikesDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(int id, int userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, id, userId);
    }

    @Override
    public void delete(int id, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, id, userId);
    }

    @Override
    public List<Integer> getAllByFilmId(int filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return jdbcTemplate.queryForList(sql, Integer.class, filmId);
    }
}