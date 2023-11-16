package ru.yandex.practicum.filmorate.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MarksDao;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Service
@Slf4j
public class FilmServiceImpl implements FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MarksDao marksDao;

    @Autowired
    public FilmServiceImpl(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                           @Qualifier("UserDbStorage") UserStorage userStorage,
                           MarksDao marksDao) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.marksDao = marksDao;
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = filmStorage.getAll();
        log.info("Возвращен список фильмов: " + films.toString());

        return films;
    }

    @Override
    public Film getFilm(int id) {
        Film film = filmStorage.getById(id);
        log.info("Возвращен фильм: " + film);

        return film;
    }

    @Override
    public Film addFilm(Film film) {
        Film newFilm = filmStorage.save(film);
        log.info("Добавлен фильм: id={}, name={}", newFilm.getId(), newFilm.getName());

        return newFilm;
    }

    @Override
    public Film updateFilm(Film film) {
        filmStorage.getById(film.getId()); // проверка наличия фильма
        filmStorage.save(film);
        log.info("Обновлен фильм: id={}, name={}", film.getId(), film.getName());

        return film;
    }

    @Override
    public void addMark(int id, int userId, int mark) {
        filmStorage.getById(id); // проверка наличия фильма
        userStorage.getById(userId); // проверка наличия пользователя
        double currentMark = marksDao.get(id, userId);
        if (currentMark == 0) {
            marksDao.save(id, userId, mark);
        } else {
            marksDao.update(id, userId, mark);
        }
        log.info("Поставлена оценка {} фильму с id={} пользователем с id={}", mark, id, userId);
    }

    @Override
    public void deleteMark(int id, int userId) {
        filmStorage.getById(id); // проверка наличия фильма
        userStorage.getById(userId); // проверка наличия пользователя
        if (!marksDao.getAllUsersByFilmId(id).contains(userId)) {
            throw new UserNotFoundException(String.format("Пользователь с id=%d не ставил оценку фильму с id=%d.",
                    userId, id));
        }
        marksDao.delete(id, userId);
        log.info("Удалена оценка фильму с id={} пользователем с id={}", id, userId);
    }

    @Override
    public List<Film> getTopRating(int count) {
        List<Film> topRating = marksDao.getTop(count);
        log.info("Возвращен список из {} фильмов с наивысшей оценкой: {}",
                topRating.size(), topRating);

        return topRating;
    }
}