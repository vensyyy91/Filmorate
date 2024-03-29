package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.dao.*;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Mapper {
    private Mapper() {
    }

    public static User makeUser(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("id");
        String email = rs.getString("email");
        String login = rs.getString("login");
        String name = rs.getString("name");
        LocalDate birthday = rs.getDate("birthday").toLocalDate();

        return new User(id, email, login, name, birthday);
    }

    public static Film makeFilm(ResultSet rs,
                                int rowNum,
                                LikesDao likesDao,
                                GenreDao genreDao,
                                MpaDao mpaDao,
                                DirectorDao directorDao) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        int duration = rs.getInt("duration");
        int rate = likesDao.getAllByFilmId(id).size();
        Set<Genre> genres = genreDao.getAllByFilmId(id);
        Mpa mpa = mpaDao.getById(rs.getInt("mpa_id"));
        Set<Director> director = directorDao.getAllByFilmId(id);

        return new Film(id, name, description, releaseDate, duration, rate, genres, mpa, director);
    }

    public static Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("genre_id");
        String name = rs.getString("genre_name");

        return new Genre(id, name);
    }

    public static Mpa makeMpa(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("mpa_id");
        String name = rs.getString("mpa_name");

        return new Mpa(id, name);
    }

    public static Director makeDirector(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("director_id");
        String name = rs.getString("director_name");

        return new Director(id, name);
    }

    public static Review makeReview(ResultSet rs, int rowNum, ReviewDao reviewDao) throws SQLException {
        int id = rs.getInt("review_id");
        String content = rs.getString("content");
        Boolean isPositive = rs.getBoolean("is_positive");
        int userId = rs.getInt("user_id");
        int filmId = rs.getInt("film_id");
        int useful = reviewDao.getReviewUsefulRating(id);

        return new Review(id, content, isPositive, userId, filmId, useful);
    }

    public static Event makeEvent(ResultSet rs, int rowNum) throws SQLException {
        int eventId = rs.getInt("event_id");
        Instant timestamp = rs.getTimestamp("create_timestamp").toInstant();
        int userId = rs.getInt("user_id");
        EventType eventType = EventType.valueOf(rs.getString("event_type"));
        Operation operation = Operation.valueOf(rs.getString("operation"));
        int entityId = rs.getInt("entity_id");

        return new Event(eventId, timestamp, userId, eventType, operation, entityId);
    }

    public static Map<String,Object> userToMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", user.getEmail());
        userMap.put("login", user.getLogin());
        userMap.put("name", user.getName());
        userMap.put("birthday", user.getBirthday());

        return userMap;
    }

    public static Map<String, Object> filmToMap(Film film) {
        Map<String, Object> filmMap = new HashMap<>();
        filmMap.put("name", film.getName());
        filmMap.put("description", film.getDescription());
        filmMap.put("release_date", film.getReleaseDate());
        filmMap.put("duration", film.getDuration());
        filmMap.put("mpa_id", film.getMpa().getId());

        return filmMap;
    }

    public static Map<String, Object> reviewToMap(Review review) {
        Map<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("content", review.getContent());
        reviewMap.put("is_positive", review.getIsPositive());
        reviewMap.put("user_id", review.getUserId());
        reviewMap.put("film_id", review.getFilmId());
        reviewMap.put("useful", review.getUseful());

        return reviewMap;
    }
}