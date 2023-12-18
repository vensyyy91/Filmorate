package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmDao {
    List<Film> getAll();

    Film getById(int id);

    Film save(Film film);

    List<Film> getTop(int count);

    List<Film> getDirectorFilms(int directorId, String sortBy);

    List<Film> getCommonFilms(int userId, int friendId);
}