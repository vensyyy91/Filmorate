package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component
@RequiredArgsConstructor
public class GenreDaoImpl implements GenreDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getAll() {
        String sql = "SELECT * FROM genres";
        return jdbcTemplate.query(sql, this::makeGenre);
    }

    @Override
    public Genre getById(int id) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::makeGenre, id);
        } catch (EmptyResultDataAccessException ex) {
            throw new GenreNotFoundException(String.format("Жанр с id=%d не найден.", id));
        }
    }

    @Override
    public Set<Genre> getAllByFilmId(int filmId) {
        String sql = "SELECT * FROM genres WHERE genre_id IN (SELECT genre_id FROM film_genre WHERE film_id = ?)";
        return new TreeSet<>(jdbcTemplate.query(sql, this::makeGenre, filmId));
    }

    private Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("genre_id");
        String name = rs.getString("genre_name");

        return new Genre(id, name);
    }
}