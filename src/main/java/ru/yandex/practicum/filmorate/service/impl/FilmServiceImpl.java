package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.dao.LikesDao;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
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
        filmDao.getById(film.getId()); // проверка наличия фильма
        filmDao.save(film);
        log.info("Обновлен фильм: id={}, name={}", film.getId(), film.getName());

        return film;
    }

    @Override
    public void like(int id, int userId) {
        filmDao.getById(id); // проверка наличия фильма
        userDao.getById(userId); // проверка наличия пользователя
        likesDao.save(id, userId);
        log.info("Поставлен лайк фильму с id={} пользователем с id={}", id, userId);
    }

    @Override
    public void deleteLike(int id, int userId) {
        filmDao.getById(id); // проверка наличия фильма
        userDao.getById(userId); // проверка наличия пользователя
        if (!likesDao.getAllByFilmId(id).contains(userId)) {
            throw new UserNotFoundException(String.format("Пользователь с id=%d не ставил лайк фильму с id=%d.",
                    userId, id));
        }
        likesDao.delete(id, userId);
        log.info("Удален лайк фильму с id={} пользователем с id={}", id, userId);
    }

    @Override
    public List<Film> getTopLikes(int count) {
        List<Film> topLikes = filmDao.getTop(count);
        log.info("Возвращен список из {} фильмов с наибольшим количеством лайков: {}", topLikes.size(), topLikes);

        return topLikes;
    }

    @Override
    public List<Film> getDirectorFilms(int directorId, String sortBy) {
        directorDao.getById(directorId); // проверка наличия режиссера
        List<Film> films = filmDao.getDirectorFilms(directorId, sortBy);
        log.info("Возвращен список всех фильмов режиссера с id={}: {}", directorId, films);

        return films;
    }
}