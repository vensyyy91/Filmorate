package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmServiceImpl implements FilmService {
    private final FilmDao filmDao;
    private final UserDao userDao;
    private final LikesDao likesDao;
    private final DirectorDao directorDao;
    private final EventDao eventDao;

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = filmDao.getAll();
        log.info("Возвращен список фильмов: {}", films);

        return films;
    }

    @Override
    public Film getFilm(int id) {
        Film film = filmDao.getById(id);
        log.info("Возвращен фильм: {}", film);

        return film;
    }

    @Override
    public Film addFilm(Film film) {
        Film newFilm = filmDao.save(film);
        log.info("Добавлен фильм: id={}, name={}", newFilm.getId(), newFilm.getName());

        return newFilm;
    }

    @Override
    public Film updateFilm(Film film) {
        checkIfFilmExists(film.getId());
        filmDao.save(film);
        log.info("Обновлен фильм: id={}, name={}", film.getId(), film.getName());

        return film;
    }

    @Override
    public void deleteFilm(int filmId) {
        checkIfFilmExists(filmId);
        filmDao.delete(filmId);
        log.info("Удален фильм с id={}", filmId);
    }

    @Override
    public void like(int id, int userId) {
        checkIfFilmExists(id);
        checkIfUserExists(userId);
        likesDao.save(id, userId);
        eventDao.writeEvent(userId, EventType.LIKE, Operation.ADD, id);
        log.info("Поставлен лайк фильму с id={} пользователем с id={}", id, userId);
    }

    @Override
    public void deleteLike(int id, int userId) {
        checkIfFilmExists(id);
        checkIfUserExists(userId);
        if (!likesDao.getAllByFilmId(id).contains(userId)) {
            throw new NotFoundException(String.format("Пользователь с id=%d не ставил лайк фильму с id=%d.",
                    userId, id));
        }
        likesDao.delete(id, userId);
        eventDao.writeEvent(userId, EventType.LIKE, Operation.REMOVE, id);
        log.info("Удален лайк фильму с id={} пользователем с id={}", id, userId);
    }

    @Override
    public List<Film> getTopLikes(int count, Integer genreId, Integer year) {
        List<Film> topLikes = filmDao.getTop(count, genreId, year);
        log.info("Возвращен список из {} фильмов с наибольшим количеством лайков: {}", topLikes.size(), topLikes);

        return topLikes;
    }

    @Override
    public List<Film> getDirectorFilms(int directorId, String sortBy) {
        checkIfDirectorExists(directorId); // проверка наличия режиссера
        List<Film> films = filmDao.getDirectorFilms(directorId, sortBy);
        log.info("Возвращен список всех фильмов режиссера с id={}: {}", directorId, films);

        return films;
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        checkIfUserExists(userId);
        checkIfUserExists(friendId);
        List<Film> films = filmDao.getCommonFilms(userId, friendId);
        log.info("Возвращен список общих фильмов для пользователей с id={} и id={}: {}", userId, friendId, films);

        return films;
    }

    @Override
    public List<Film> search(String query, String by) {
        List<Film> films = filmDao.search(query, by);
        log.info("Возвращен результат поиска: {}", films);
        return films;
    }

    private void checkIfFilmExists(int id) {
        filmDao.getById(id);
    }

    private void checkIfUserExists(int id) {
        userDao.getById(id);
    }

    private void checkIfDirectorExists(int id) {
        directorDao.getById(id);
    }
}