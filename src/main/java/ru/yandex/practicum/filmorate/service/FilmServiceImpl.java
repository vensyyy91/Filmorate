package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.dao.LikesDao;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmServiceImpl implements FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikesDao likesDao;
    private final GenreDao genreDao;
    private final MpaDao mpaDao;

    @Autowired
    public FilmServiceImpl(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                           @Qualifier("UserDbStorage") UserStorage userStorage,
                           LikesDao likesDao, GenreDao genreDao, MpaDao mpaDao) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likesDao = likesDao;
        this.genreDao = genreDao;
        this.mpaDao = mpaDao;
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
        log.info(String.format("Добавлен фильм: id=%d, name=%s", newFilm.getId(), newFilm.getName()));

        return newFilm;
    }

    @Override
    public Film updateFilm(Film film) {
        filmStorage.getById(film.getId()); // проверка наличия фильма
        filmStorage.save(film);
        log.info(String.format("Обновлен фильм: id=%d, name=%s", film.getId(), film.getName()));

        return film;
    }

    @Override
    public void like(int id, int userId) {
        filmStorage.getById(id); // проверка наличия фильма
        userStorage.getById(userId); // проверка наличия пользователя
        likesDao.save(id, userId);
        log.info(String.format("Поставлен лайк фильму с id=%d пользователем с id=%d", id, userId));
    }

    @Override
    public void deleteLike(int id, int userId) {
        filmStorage.getById(id); // проверка наличия фильма
        userStorage.getById(userId); // проверка наличия пользователя
        if (!likesDao.getAllByFilmId(id).contains(userId)) {
            throw new UserNotFoundException(String.format("Пользователь с id=%d не ставил лайк фильму с id=%d.",
                    userId, id));
        }
        likesDao.delete(id, userId);
        log.info(String.format("Удален лайк фильму с id=%d пользователем с id=%d", id, userId));
    }

    @Override
    public List<Film> getTopLikes(int count) {
        List<Film> topLikes = likesDao.getTop(count).stream().map(filmStorage::getById).collect(Collectors.toList());
        log.info(String.format("Возвращен список из %d фильмов с наибольшим количеством лайков: %s",
                topLikes.size(), topLikes));

        return topLikes;
    }

    @Override
    public List<Genre> getAllGenres() {
        List<Genre> genres = genreDao.getAll();
        log.info("Возвращен список жанров: " + genres.toString());

        return genres;
    }

    @Override
    public Genre getGenreById(int id) {
        Genre genre = genreDao.getById(id);
        log.info("Возвращен жанр: " + genre);

        return genre;
    }

    @Override
    public List<Mpa> getAllMpa() {
        List<Mpa> mpaList = mpaDao.getAll();
        log.info("Возвращен список рейтингов: " + mpaList.toString());

        return mpaList;
    }

    @Override
    public Mpa getMpaById(int id) {
        Mpa mpa = mpaDao.getById(id);
        log.info("Возвращен рейтинг: " + mpa);

        return mpa;
    }
}