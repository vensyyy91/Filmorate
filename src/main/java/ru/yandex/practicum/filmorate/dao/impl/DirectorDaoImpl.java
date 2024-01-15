package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.util.Mapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component
@RequiredArgsConstructor
public class DirectorDaoImpl implements DirectorDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getAll() {
        String sql = "SELECT * FROM directors";
        return jdbcTemplate.query(sql, Mapper::makeDirector);
    }

    @Override
    public Director getById(int id) {
        String sql = "SELECT * FROM directors WHERE director_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Mapper::makeDirector, id);
        } catch (EmptyResultDataAccessException ex) {
            throw new NotFoundException(String.format("Режиссер с id=%d не найден.", id));
        }
    }

    @Override
    public Director save(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("director_id");
        int id = simpleJdbcInsert.executeAndReturnKey(Collections.singletonMap("director_name", director.getName())).intValue();
        director.setId(id);

        return director;
    }

    @Override
    public Director update(Director director) {
        String sql = "UPDATE directors SET director_name = ? WHERE director_id = ?";
        jdbcTemplate.update(sql, director.getName(), director.getId());

        return director;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM directors where director_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Set<Director> getAllByFilmId(int filmId) {
        String sql = "SELECT * FROM directors WHERE director_id IN (SELECT director_id FROM film_director WHERE film_id = ?)";
        return new TreeSet<>(jdbcTemplate.query(sql, Mapper::makeDirector, filmId));
    }
}