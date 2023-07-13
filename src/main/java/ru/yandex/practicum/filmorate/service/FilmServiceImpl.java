package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmServiceImpl implements FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private int id;

    @Autowired
    public FilmServiceImpl(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @Override
    public List<Film> getAllFilms() {
        log.info("Возвращен список фильмов: " + filmStorage.getAll().toString());
        return filmStorage.getAll();
    }

    @Override
    public Film getFilm(int id) {
        log.info("Возвращен фильм: " + filmStorage.get(id));
        return filmStorage.get(id);
    }

    @Override
    public Film addFilm(Film film) {
        int id = generateID();
        film.setId(id);
        filmStorage.save(id, film);
        log.info(String.format("Добавлен фильм: id=%d, name=%s", id, film.getName()));
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        int id = film.getId();
        if (filmStorage.getAll().stream().noneMatch(f -> f.getId() == id)) {
            throw new FilmNotFoundException(String.format("Фильма с id=%d не существует.", id));
        }
        filmStorage.save(id, film);
        log.info(String.format("Обновлен фильм: id=%d, name=%s", id, film.getName()));
        return film;
    }

    @Override
    public void like(int id, int userId) {
        Film film = filmStorage.get(id);
        userStorage.get(userId); // выбросит исключение, если пользователя с таким id не существует
        film.getLikes().add(userId);
        film.setLikesCount(film.getLikesCount() + 1);
        log.info(String.format("Поставлен лайк фильму с id=%d пользователем с id=%d", id, userId));
    }

    @Override
    public void deleteLike(int id, int userId) {
        Film film = filmStorage.get(id);
        userStorage.get(userId); // выбросит исключение, если пользователя с таким id не существует
        if (!film.getLikes().contains(userId)) {
            throw new UserNotFoundException(String.format("Пользователь с id=%d не ставил лайк фильму с id=%d.",
                    userId, id));
        }
        film.getLikes().remove(userId);
        film.setLikesCount(film.getLikesCount() - 1);
        log.info(String.format("Удален лайк фильму с id=%d пользователем с id=%d", id, userId));
    }

    @Override
    public List<Film> getTopLikes(int count) {
        int size = Math.min(count, filmStorage.getAll().size());
        List<Film> topLikes = filmStorage.getAll().stream()
                .sorted(Comparator.comparing(Film::getLikesCount).reversed())
                .limit(size)
                .collect(Collectors.toList());
        log.info(String.format("Возвращен список из %d фильмов с наибольшим количеством лайков: %s",
                size, topLikes));

        return topLikes;
    }

    private int generateID() {
        return ++id;
    }
}