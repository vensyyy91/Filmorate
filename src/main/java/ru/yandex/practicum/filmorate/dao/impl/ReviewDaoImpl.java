package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.ReviewDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.util.Mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewDaoImpl implements ReviewDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Review> getAll(Integer filmId, int count) {
        StringBuilder sql = new StringBuilder("SELECT * FROM reviews");
        if (filmId != null) {
            sql.append(" WHERE film_id = ").append(filmId);
        }
        sql.append(" LIMIT ?");

        return jdbcTemplate.query(sql.toString(), this::makeReview, count);
    }

    @Override
    public Review getById(int id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::makeReview, id);
        } catch (EmptyResultDataAccessException ex) {
            throw new NotFoundException(String.format("Обзор с id=%d не найден.", id));
        }
    }

    @Override
    public Review save(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");
        int id = simpleJdbcInsert.executeAndReturnKey(Mapper.reviewToMap(review)).intValue();
        review.setReviewId(id);

        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());

        return getById(review.getReviewId());
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addLike(int id, int userId) {
        String sql = "INSERT INTO review_like (user_id, review_id, is_positive) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, id, true);
    }

    @Override
    public void addDislike(int id, int userId) {
        String sql = "INSERT INTO review_like (user_id, review_id, is_positive) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, id, false);
    }

    @Override
    public void deleteLikeOrDislike(int id, int userId) {
        String sql = "DELETE FROM review_like WHERE user_id = ? AND review_id = ?";
        jdbcTemplate.update(sql, userId, id);
    }

    @Override
    public int getReviewUsefulRating(int id) {
        String sqlPositive = "SELECT COUNT(*) FROM review_like WHERE review_id = ? AND is_positive = true";
        Integer positive = Optional.ofNullable(jdbcTemplate.queryForObject(sqlPositive, Integer.class, id)).orElse(0);
        String sqlNegative = "SELECT COUNT(*) FROM review_like WHERE review_id = ? AND is_positive = false";
        Integer negative = Optional.ofNullable(jdbcTemplate.queryForObject(sqlNegative, Integer.class, id)).orElse(0);

        return positive - negative;
    }

    private Review makeReview(ResultSet rs, int rowNum) throws SQLException {
        return Mapper.makeReview(rs, rowNum, this);
    }
}