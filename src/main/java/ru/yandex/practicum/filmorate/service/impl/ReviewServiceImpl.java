package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.dao.ReviewDao;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final ReviewDao reviewDao;
    private final UserDao userDao;
    private final FilmDao filmDao;
    private final EventDao eventDao;

    @Override
    public Review addReview(Review review) {
        userDao.getById(review.getUserId()); // проверка наличия пользователя
        filmDao.getById(review.getFilmId()); // проверка наличия фильма
        Review newReview = reviewDao.save(review);
        eventDao.writeEvent(newReview.getUserId(), EventType.REVIEW, Operation.ADD, newReview.getReviewId());
        log.info("Добавлен отзыв от пользователя с id={} фильму с id={}: id={}, content={}",
                newReview.getUserId(),
                newReview.getFilmId(),
                newReview.getReviewId(),
                newReview.getContent());

        return newReview;
    }

    @Override
    public Review updateReview(Review review) {
        reviewDao.getById(review.getReviewId()); // проверка наличия отзыва
        userDao.getById(review.getUserId()); // проверка наличия пользователя
        filmDao.getById(review.getFilmId()); // проверка наличия фильма
        Review newReview = reviewDao.update(review);
        eventDao.writeEvent(newReview.getUserId(), EventType.REVIEW, Operation.UPDATE, newReview.getReviewId());
        log.info("Обновлен отзыв от пользователя с id={} фильму с id={}: id={}, content={}",
                newReview.getUserId(),
                newReview.getFilmId(),
                newReview.getReviewId(),
                newReview.getContent());

        return newReview;
    }

    @Override
    public void deleteReview(int id) {
        Review review = reviewDao.getById(id);
        int userId = review.getUserId();
        reviewDao.delete(id);
        eventDao.writeEvent(userId, EventType.REVIEW, Operation.REMOVE, id);
        log.info("Удален отзыв с id={}", id);
    }

    @Override
    public Review getReviewById(int id) {
        Review review = reviewDao.getById(id);
        log.info("Возвращен отзыв: {}", review);

        return review;
    }

    @Override
    public List<Review> getAllReviews(Integer filmId, int count) {
        List<Review> reviews = reviewDao.getAll(filmId, count).stream()
                .sorted(Comparator.comparingInt(Review::getUseful).reversed())
                .collect(Collectors.toList());
        log.info("Возвращен список отзывов: {}", reviews);

        return reviews;
    }

    @Override
    public void addLike(int id, int userId) {
        userDao.getById(userId); // проверка наличия пользователя
        reviewDao.getById(id); // проверка наличия отзыва
        reviewDao.addLike(id, userId);
        log.info("Пользователем с id={} поставлен лайк отзыву с id={}", userId, id);
    }

    @Override
    public void addDislike(int id, int userId) {
        userDao.getById(userId); // проверка наличия пользователя
        reviewDao.getById(id); // проверка наличия отзыва
        reviewDao.addDislike(id, userId);
        log.info("Пользователем с id={} поставлен дизлайк отзыву с id={}", userId, id);
    }

    @Override
    public void deleteLikeOrDislike(int id, int userId) {
        userDao.getById(userId); // проверка наличия пользователя
        reviewDao.getById(id); // проверка наличия отзыва
        reviewDao.deleteLikeOrDislike(id, userId);
        log.info("Пользователем с id={} удален лайк/дизлайк отзыву с id={}", userId, id);
    }
}