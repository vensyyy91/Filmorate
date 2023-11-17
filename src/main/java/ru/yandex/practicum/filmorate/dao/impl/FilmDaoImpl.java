package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.dao.LikesDao;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.util.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Component("FilmDbStorage")
@RequiredArgsConstructor
public class FilmDaoImpl implements FilmDao {
    private final JdbcTemplate jdbcTemplate;
    private final GenreDao genreDao;
    private final LikesDao likesDao;
    private final MpaDao mpaDao;

    @Override
    public List<Film> getAll() {
        String sql = "SELECT * FROM films";
        return jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.makeFilm(rs, rowNum, likesDao, genreDao, mpaDao));
    }

    @Override
    public Film getById(int id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> Mapper.makeFilm(rs, rowNum, likesDao, genreDao, mpaDao), id);
        } catch (EmptyResultDataAccessException ex) {
            throw new FilmNotFoundException(String.format("Фильм с id=%d не найден.", id));
        }
    }

    @Override
    public Film save(Film film) {
        if (film.getId() == 0) {
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("films")
                    .usingGeneratedKeyColumns("id");
            int id = simpleJdbcInsert.executeAndReturnKey(Mapper.filmToMap(film)).intValue();
            film.setId(id);
            String sqlAddFilm = "INSERT INTO film_genre (film_id, genre_id) VALUES (" + film.getId() + ", ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sqlAddFilm, genre.getId());
            }
        } else {
            String sqlUpdateFilm = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, " +
                    "mpa_id = ? WHERE id = ?";
            jdbcTemplate.update(sqlUpdateFilm, film.getName(), film.getDescription(), film.getReleaseDate(),
                    film.getDuration(), film.getMpa().getId(), film.getId());
            List<Integer> genres = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toList());
            List<Integer> genresFromDb = genreDao.getAllByFilmId(film.getId()).stream()
                    .map(Genre::getId)
                    .collect(Collectors.toList());
            String sqlAddGenre = "INSERT INTO film_genre (film_id, genre_id) VALUES (" + film.getId() + ", ?)";
            String sqlDeleteGenre = "DELETE FROM film_genre WHERE film_id = " + film.getId() + " AND genre_id = ?";
            for (Integer genreId : genres) {
                if (genresFromDb.contains(genreId)) {
                    genresFromDb.remove(genreId);
                } else {
                    jdbcTemplate.update(sqlAddGenre, genreId);
                }
            }
            if (!genresFromDb.isEmpty()) {
                for (Integer genreId : genresFromDb) {
                    jdbcTemplate.update(sqlDeleteGenre, genreId);
                }
            }
            film.setRate(likesDao.getAllByFilmId(film.getId()).size());
        }
        film.setGenres(genreDao.getAllByFilmId(film.getId()));
        film.setMpa(mpaDao.getById(film.getMpa().getId()));

        return film;
    }
}