package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.util.Mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FilmDaoImpl implements FilmDao {
    private final JdbcTemplate jdbcTemplate;
    private final GenreDao genreDao;
    private final LikesDao likesDao;
    private final MpaDao mpaDao;
    private final DirectorDao directorDao;

    @Override
    public List<Film> getAll() {
        String sql = "SELECT * FROM films";
        return jdbcTemplate.query(sql, this::makeFilm);
    }

    @Override
    public Film getById(int id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::makeFilm, id);
        } catch (EmptyResultDataAccessException ex) {
            throw new NotFoundException(String.format("Фильм с id=%d не найден.", id));
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
            if (!film.getGenres().isEmpty()) {
                jdbcTemplate.update(getSqlForGenres(film));
            }
            if (!film.getDirectors().isEmpty()) {
                jdbcTemplate.update(getSqlForDirectors(film));
            }
        } else {
            String sqlUpdateFilm = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, " +
                    "mpa_id = ? WHERE id = ?";
            jdbcTemplate.update(sqlUpdateFilm, film.getName(), film.getDescription(), film.getReleaseDate(),
                    film.getDuration(), film.getMpa().getId(), film.getId());
            List<Integer> genres = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toList());
            updateGenres(film.getId(), genres);
            List<Integer> directors = film.getDirectors().stream()
                    .map(Director::getId)
                    .collect(Collectors.toList());
            updateDirectors(film.getId(), directors);
            film.setRate(likesDao.getAllByFilmId(film.getId()).size());
        }
        film.setGenres(genreDao.getAllByFilmId(film.getId()));
        film.setDirectors(directorDao.getAllByFilmId(film.getId()));
        film.setMpa(mpaDao.getById(film.getMpa().getId()));

        return film;
    }

    @Override
    public void delete(int filmId) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    @Override
    public List<Film> getTop(int count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder("SELECT f.* FROM films AS f LEFT JOIN likes AS l ON f.id = l.film_id ");
        if (genreId != null && year != null) {
            sql.append("LEFT JOIN film_genre AS fg ON f.id = fg.film_id WHERE fg.genre_id = ")
                    .append(genreId)
                    .append(" AND EXTRACT(YEAR FROM f.release_date) = ")
                    .append(year)
                    .append(" ");
        } else if (genreId != null) {
            sql.append("LEFT JOIN film_genre AS fg ON f.id = fg.film_id WHERE fg.genre_id = ").append(genreId).append(" ");
        } else if (year != null) {
            sql.append("WHERE EXTRACT(YEAR FROM f.release_date) = ").append(year).append(" ");
        }
        sql.append("GROUP BY f.id ORDER BY COUNT(l.user_id) DESC LIMIT ?");

        return jdbcTemplate.query(sql.toString(), this::makeFilm, count);
    }

    @Override
    public List<Film> getDirectorFilms(int directorId, String sortBy) {
        String sql;
        if (sortBy == null) {
            sql = "SELECT * FROM films WHERE id IN (" +
                    "SELECT film_id FROM film_director WHERE director_id = ?)";
        } else if (sortBy.equals("year")) {
            sql = "SELECT * FROM films WHERE id IN (" +
                    "SELECT film_id FROM film_director WHERE director_id = ?" +
                    ") ORDER BY EXTRACT(YEAR FROM release_date)";
        } else if (sortBy.equals("likes")) {
            sql = "SELECT f.* FROM films AS f LEFT JOIN likes AS l ON f.id = l.film_id WHERE f.id IN (" +
                    "SELECT film_id FROM film_director WHERE director_id = ?" +
                    ") GROUP BY f.id ORDER BY COUNT(l.user_id) DESC";
        } else {
            throw new IllegalArgumentException("Параметр сортировки должен быть year или likes");
        }

        return jdbcTemplate.query(sql, this::makeFilm, directorId);
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sql = "SELECT f.*, COUNT(l.user_id) AS rate " +
                "FROM films AS f " +
                "LEFT JOIN likes AS l ON f.id = l.film_id " +
                "WHERE f.id IN (" +
                    "SELECT film_id " +
                    "FROM likes " +
                    "WHERE user_id = ? OR user_id = ? " +
                    "GROUP BY film_id " +
                    "HAVING COUNT(user_id) = 2" +
                ") " +
                "GROUP BY f.id " +
                "ORDER BY rate DESC";

        return jdbcTemplate.query(sql, this::makeFilm, userId, friendId);
    }

    @Override
    public List<Film> search(String query, String by) {
        query = "%" + query.toLowerCase() + "%";
        List<Film> result;
        switch (by) {
            case "director,title":
            case "title,director":
                result = searchByDirectorAndTitle(query);
                break;
            case "director":
                result = searchByDirector(query);
                break;
            case "title":
                result = searchByTitle(query);
                break;
            default:
                throw new IllegalArgumentException("Некорректный параметр by, укажите в параметре title, director или оба варианта.");
        }

        return result;
    }

    @Override
    public List<Film> getRecommendations(int id) {
        List<Integer> userIdWithCommonLikes = likesDao.getUserIdWithCommonLikes(id);
        StringJoiner userIdList = new StringJoiner(",");
        for (Integer userId : userIdWithCommonLikes) {
            userIdList.add(userId.toString());
        }
        String sql = "SELECT f.* " +
                "FROM films AS f " +
                "LEFT JOIN likes AS l ON f.id = l.film_id " +
                "WHERE l.user_id IN (" + userIdList +
                ") AND f.id NOT IN (" +
                    "SELECT film_id " +
                    "FROM likes " +
                    "WHERE user_id = ?" +
                ") " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(l.user_id)";

        return jdbcTemplate.query(sql, this::makeFilm, id);
    }

    private Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        return Mapper.makeFilm(rs, rowNum, likesDao, genreDao, mpaDao, directorDao);
    }

    private String getSqlForGenres(Film film) {
        StringJoiner sqlGenres = new StringJoiner(", ");
        for (Genre genre : film.getGenres()) {
            sqlGenres.add(String.format("(%d, %d)", film.getId(), genre.getId()));
        }

        return "INSERT INTO film_genre (film_id, genre_id) VALUES " + sqlGenres;
    }

    private String getSqlForDirectors(Film film) {
        StringJoiner sqlDirectors = new StringJoiner(", ");
        for (Director director : film.getDirectors()) {
            sqlDirectors.add(String.format("(%d, %d)", film.getId(), director.getId()));
        }

        return "INSERT INTO film_director (film_id, director_id) VALUES " + sqlDirectors;
    }

    private void updateGenres(int filmId, List<Integer> genres) {
        List<Integer> genresFromDb = genreDao.getAllByFilmId(filmId).stream()
                .map(Genre::getId)
                .collect(Collectors.toList());
        String sqlAddGenre = "INSERT INTO film_genre (film_id, genre_id) VALUES (" + filmId + ", ?)";
        String sqlDeleteGenre = "DELETE FROM film_genre WHERE film_id = " + filmId + " AND genre_id = ?";
        updateDatabase(genres, genresFromDb, sqlAddGenre, sqlDeleteGenre);
    }

    private void updateDirectors(int filmId, List<Integer> directors) {
        List<Integer> directorsFromDb = directorDao.getAllByFilmId(filmId).stream()
                .map(Director::getId)
                .collect(Collectors.toList());
        String sqlAddDirector = "INSERT INTO film_director (film_id, director_id) VALUES (" + filmId + ", ?)";
        String sqlDeleteDirector = "DELETE FROM film_director WHERE film_id = " + filmId + " AND director_id = ?";
        updateDatabase(directors, directorsFromDb, sqlAddDirector, sqlDeleteDirector);
    }

    private void updateDatabase(List<Integer> newList, List<Integer> listFromDb, String sqlAdd, String sqlDelete) {
        for (Integer id : newList) {
            if (listFromDb.contains(id)) {
                listFromDb.remove(id);
            } else {
                jdbcTemplate.update(sqlAdd, id);
            }
        }
        if (!listFromDb.isEmpty()) {
            for (Integer id : listFromDb) {
                jdbcTemplate.update(sqlDelete, id);
            }
        }
    }

    private List<Film> searchByDirectorAndTitle(String query) {
        String sql = "SELECT f.* FROM films AS f " +
                "LEFT JOIN likes AS l ON f.id = l.film_id " +
                "LEFT JOIN film_director AS fd ON f.id = fd.film_id " +
                "LEFT JOIN directors AS d ON fd.director_id = d.director_id " +
                "WHERE LOWER(d.director_name) LIKE ? " +
                "OR LOWER(f.name) LIKE ? " +
                "GROUP BY f.id ORDER BY COUNT(l.user_id) DESC";
        return jdbcTemplate.query(sql, this::makeFilm, query, query);
    }

    private List<Film> searchByDirector(String query) {
        String sql = "SELECT f.* FROM films AS f " +
                "LEFT JOIN likes AS l ON f.id = l.film_id " +
                "LEFT JOIN film_director AS fd ON f.id = fd.film_id " +
                "LEFT JOIN directors AS d ON fd.director_id = d.director_id " +
                "WHERE LOWER(d.director_name) LIKE ? " +
                "GROUP BY f.id ORDER BY COUNT(l.user_id) DESC";
        return jdbcTemplate.query(sql, this::makeFilm, query);
    }

    private List<Film> searchByTitle(String query) {
        String sql = "SELECT f.* FROM films AS f " +
                "LEFT JOIN likes AS l ON f.id = l.film_id " +
                "WHERE LOWER(name) LIKE ? " +
                "GROUP BY f.id ORDER BY COUNT(l.user_id) DESC";
        return jdbcTemplate.query(sql, this::makeFilm, query);
    }
}