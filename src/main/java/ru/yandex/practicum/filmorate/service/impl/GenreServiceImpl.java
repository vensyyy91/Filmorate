package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {
    private final GenreDao genreDao;

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
}