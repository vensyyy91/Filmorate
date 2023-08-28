package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MpaServiceImpl implements MpaService {
    private final MpaDao mpaDao;

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