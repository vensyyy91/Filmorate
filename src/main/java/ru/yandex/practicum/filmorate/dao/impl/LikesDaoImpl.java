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

    @Override
    public List<Integer> getUserIdWithCommonLikes(int userId) {
        String sqlCommonLikes = "SELECT l2.user_id " +
                "FROM likes AS l1, likes AS l2 " +
                "WHERE l1.film_id = l2.film_id " +
                "AND l1.user_id = ? " +
                "AND l1.user_id <> l2.user_id " +
                "GROUP BY l2.user_id " +
                "HAVING COUNT(l2.film_id) > 0 " +
                "ORDER BY COUNT(l2.film_id) DESC";
        return jdbcTemplate.queryForList(sqlCommonLikes, Integer.class, userId);
    }
}