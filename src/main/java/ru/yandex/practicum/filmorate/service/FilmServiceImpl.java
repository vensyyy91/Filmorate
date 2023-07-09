package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmServiceImpl implements FilmService {
    private final Repository<Film> repository;
    private final UserService userService;
    private int id;

    @Autowired
    public FilmServiceImpl(Repository<Film> repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    @Override
    public List<Film> getAllFilms() {
        log.info("Возвращен список фильмов: " + repository.getAll().toString());
        return repository.getAll();
    }

    @Override
    public Film getFilm(int id) {
        log.info("Возвращен фильм: " + repository.get(id));
        return repository.get(id);
    }

    @Override
    public Film addFilm(Film film) {
        int id = generateID();
        film.setId(id);
        repository.save(id, film);
        log.info(String.format("Добавлен фильм: id=%d, name=%s", id, film.getName()));
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        int id = film.getId();
        if (repository.getAll().stream().noneMatch(f -> f.getId() == id)) {
            throw new FilmNotFoundException(String.format("Фильма с id=%d не существует.", id));
        }
        repository.save(id, film);
        log.info(String.format("Обновлен фильм: id=%d, name=%s", id, film.getName()));
        return film;
    }

    @Override
    public void like(int id, int userId) {
        Film film = repository.get(id);
        userService.getUser(userId); // выбросит исключение, если пользователя с таким id не существует
        film.getLikes().add(userId);
        log.info(String.format("Поставлен лайк фильму с id=%d пользователем с id=%d", id, userId));
    }

    @Override
    public void deleteLike(int id, int userId) {
        Film film = repository.get(id);
        userService.getUser(userId); // выбросит исключение, если пользователя с таким id не существует
        if (!film.getLikes().contains(userId)) {
            throw new UserNotFoundException(String.format("Пользователь с id=%d не ставил лайк фильму с id=%d.",
                    userId, id));
        }
        film.getLikes().remove(userId);
        log.info(String.format("Удален лайк фильму с id=%d пользователем с id=%d", id, userId));
    }

    @Override
    public List<Film> getTopLikes(int count) {
        int size = Math.min(count, repository.getAll().size());
        List<Film> topLikes = repository.getAll().stream()
                .sorted(Comparator.comparing(Film::getLikes, (l1, l2) -> l2.size() - l1.size()))
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