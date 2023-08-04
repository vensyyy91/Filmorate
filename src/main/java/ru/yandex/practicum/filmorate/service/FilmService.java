package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface FilmService {
    List<Film> getAllFilms();

    Film getFilm(int id);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    void like(int id, int userId);

    void deleteLike(int id, int userId);

    List<Film> getTopLikes(int count);

    List<Genre> getAllGenres();

    Genre getGenreById(int id);

    List<Mpa> getAllMpa();

    Mpa getMpaById(int id);
}