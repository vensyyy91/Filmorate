package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MpaDaoImpl implements MpaDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> getAll() {
        String sql = "SELECT * FROM mpa";
        return jdbcTemplate.query(sql, this::makeMpa);
    }

    @Override
    public Mpa getById(int id) {
        String sql = "SELECT * FROM mpa WHERE mpa_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::makeMpa, id);
        } catch (EmptyResultDataAccessException ex) {
            throw new MpaNotFoundException(String.format("Рейтинг с id=%d не найден.", id));
        }
    }

    private Mpa makeMpa(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("mpa_id");
        String name = rs.getString("mpa_name");

        return new Mpa(id, name);
    }
}