package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectorServiceImpl implements DirectorService {
    private final DirectorDao directorDao;

    @Override
    public List<Director> getAllDirectors() {
        List<Director> directors = directorDao.getAll();
        log.info("Возвращен список режиссеров: {}", directors);

        return directors;
    }

    @Override
    public Director getDirectorById(int id) {
        Director director = directorDao.getById(id);
        log.info("Возвращен режиссер: {}", director);

        return director;
    }

    @Override
    public Director addDirector(Director director) {
        Director newDirector = directorDao.save(director);
        log.info("Добавлен режиссер: id={}, name={}", newDirector.getId(), newDirector.getName());

        return newDirector;
    }

    @Override
    public Director updateDirector(Director director) {
        checkIfDirectorExists(director.getId());
        directorDao.update(director);
        log.info("Обновлен режиссер: id={}, name={}", director.getId(), director.getName());

        return director;
    }

    @Override
    public void deleteDirector(int id) {
        checkIfDirectorExists(id);
        directorDao.delete(id);
        log.info("Удален пользователь с id={}", id);
    }

    private void checkIfDirectorExists(int id) {
        directorDao.getById(id);
    }
}