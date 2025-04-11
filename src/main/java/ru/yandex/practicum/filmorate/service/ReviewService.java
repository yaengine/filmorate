package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewDbStorage;

import java.util.List;

@Slf4j
@Service
public class ReviewService {

    private final ReviewDbStorage storage;

    public ReviewService(ReviewDbStorage storage) {
        this.storage = storage;
    }

    public Review addReview(Review review) {
        check(review);
        try {
            return storage.addReview(review);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при добавлении. " + e.getMessage());
        }
    }

    public Review updateReview(Review review) {
        check(review);
        try {
            return storage.updateReview(review);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при обновлении. " + e.getMessage());
        }
    }

    public void deleteReview(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("Invalid review ID: " + id);
        }
        try {
            storage.deleteReview(id);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении. " + e.getMessage());
        }
    }

    public Review getReviewById(Long id) {
        return storage.getReviewById(id).orElseThrow();
    }

    public List<Review> getReviewByFilm(Long filmId, int count) {
        if (filmId == null || filmId <= 0) {
            throw new ValidationException("Invalid review ID: " + filmId);
        }
        try {
            return storage.getReviewByFilm(filmId, count);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении отзывов о фильме. " + e.getMessage());
        }
    }

    public List<Review> getAllReviews(int count) {
        try {
            return storage.getAllReviews(count);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении всех отзывов. " + e.getMessage());
        }
    }

    public void likeToReview(Long reviewId, Long userId) {
        checkId(reviewId, userId);
        try {
            storage.likeOrDislikeToReview(reviewId, userId, true);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при добавлении лайка отзыву. " + e.getMessage());
        }
    }

    public void dislikeToReview(Long reviewId, Long userId) {
        checkId(reviewId, userId);
        try {
            storage.likeOrDislikeToReview(reviewId, userId, false);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при добавлении дизлайка к отзыву. " + e.getMessage());
        }
    }

    public void deleteLike(Long reviewId, Long userId) {
        checkId(reviewId, userId);
        storage.deleteLikeOrDislike(reviewId, userId, true);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        checkId(reviewId, userId);
        storage.deleteLikeOrDislike(reviewId, userId, false);
    }

    private void check(Review review) {
        if (review.getUserId() <= 0) {
            throw new NotFoundException("Пользователь не найден.");
        }
        if (review.getFilmId() <= 0) {
            throw new NotFoundException("Фильм не найден.");
        }
        if (review.getIsPositive() == null) {
            throw new ValidationException("isPositive is null");
        }
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Content должен быть заполнен");
        }
    }

    private void checkId(Long reviewId, Long userId) {
        if (reviewId == null || reviewId <= 0) {
            throw new ValidationException("ID отзыва должен быть положительным числом.");
        }
        if (userId == null || userId <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным числом.");
        }
    }
}
