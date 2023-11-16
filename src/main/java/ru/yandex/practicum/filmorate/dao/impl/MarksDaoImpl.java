package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.dao.MarksDao;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.util.Mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MarksDaoImpl implements MarksDao {
    private final JdbcTemplate jdbcTemplate;
    private final GenreDao genreDao;
    private final MpaDao mpaDao;

    @Override
    public void save(int id, int userId, int mark) {
        String sql = "INSERT INTO marks (film_id, user_id, mark) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, id, userId, mark);
    }

    @Override
    public void update(int id, int userId, int mark) {
        String sql = "UPDATE marks SET mark = ? WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, mark, id, userId);
    }

    @Override
    public void delete(int id, int userId) {
        String sql = "DELETE FROM marks WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, id, userId);
    }

    @Override
    public Integer get(int filmId, int userId) {
        String sql = "SELECT mark FROM marks WHERE film_id = ? AND user_id =?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);
        } catch (EmptyResultDataAccessException ex) {
            return 0;
        }
    }

    @Override
    public List<Film> getTop(int count) {
        String sql = "SELECT f.* FROM films AS f " +
                "LEFT JOIN marks AS m ON f.id = m.film_id " +
                "GROUP BY f.id ORDER BY AVG(m.mark) DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::makeFilm, count);
    }

    @Override
    public List<Integer> getAllUsersByFilmId(int filmId) {
        String sql = "SELECT user_id FROM marks WHERE film_id = ?";
        return jdbcTemplate.queryForList(sql, Integer.class, filmId);
    }

    @Override
    public Double getFilmRate(int filmId) {
        String sql = "SELECT ROUND(AVG(mark), 2) FROM marks WHERE film_id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Double.class, filmId)).orElse(0.0);
    }

    private Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        return Mapper.makeFilm(rs, this, genreDao, mpaDao);
    }
}