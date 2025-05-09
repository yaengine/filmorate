package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ReviewDbStorage {

    private final JdbcTemplate jdbc;
    private final ReviewRowMapper reviewMapper;
    private final FeedDbStorage feedDbStorage;

    @Transactional
    public Review addReview(Review review) {
        String addQuery = "INSERT INTO reviews (content, is_positive, user_id, film_id) " +
                "VALUES (?, ?, ?, ?)";
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(addQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, key);
        Long reviewId;
        if (Objects.nonNull(key.getKey())) {
            reviewId = key.getKey().longValue();
        } else {
            throw new ValidationException("Ошибка присвоения id отзыву");
        }
        review.setReviewId(reviewId);
        feedDbStorage.createFeed(review.getUserId(), reviewId, EventType.REVIEW, Operation.ADD);
        log.info("Создан отзыв {} userId {} к фильму filmId {}", getReviewById(reviewId).get(), review.getUserId(), review.getFilmId());
        return getReviewById(reviewId).orElseThrow();
    }

    public Review updateReview(Review review) {
        String updateQuery = "UPDATE reviews SET content = ?, is_positive = ? " +
                "WHERE review_id = ?";
        int update = jdbc.update(updateQuery,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );
        if (update == 0) {
            log.info("Не удалось обновить отзыв с id {}.", review.getReviewId());
            throw new NotFoundException("Отзыв не найден.");
        }
        review = getReviewById(review.getReviewId()).orElseThrow();
        feedDbStorage.createFeed(review.getUserId(), review.getReviewId(), EventType.REVIEW, Operation.UPDATE);
        log.info("Отзыв обновлен {} ", getReviewById(review.getReviewId()).get());
        return review;
    }

    public void deleteReview(Long id) {
        Review review = getReviewById(id).orElseThrow();
        String deleteUsefulQuery = "DELETE FROM useful WHERE review_id = ?";
        jdbc.update(deleteUsefulQuery, id);
        String deleteReviewQuery = "DELETE FROM reviews WHERE review_id = ?";
        jdbc.update(deleteReviewQuery, id);
        feedDbStorage.createFeed(review.getUserId(), id, EventType.REVIEW, Operation.REMOVE);
        log.info("Отзыв {} удален  ", id);
    }

    public Optional<Review> getReviewById(Long id) {
        String query = "SELECT r.review_id, r.content, r.is_positive, u.user_name, f.film_name, " +
                "r.user_id, r.film_id, " +
                "COALESCE(SUM(CASE WHEN uf.is_like = TRUE THEN 1 ELSE 0 END), 0) AS likes_count, " +
                "COALESCE(SUM(CASE WHEN uf.is_like = FALSE THEN 1 ELSE 0 END), 0) AS dislikes_count " +
                "FROM reviews r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "JOIN films f ON r.film_id = f.film_id " +
                "LEFT JOIN useful uf ON r.review_id = uf.review_id " +
                "WHERE r.review_id = ? " +
                "GROUP BY r.review_id, r.content, r.is_positive, u.user_name, f.film_name, r.user_id, r.film_id";
        try {
            Review result = jdbc.queryForObject(query, reviewMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Отзыв с id " + id + " не найден");
        }
    }

    public List<Review> getReviewByFilm(Long filmId, int count) {
        String reviewByFilm = "SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, " +
                "COALESCE(likes.likes_count, 0) AS likes_count, " +
                "COALESCE(dislikes.dislikes_count, 0) AS dislikes_count " +
                "FROM reviews r " +
                "LEFT JOIN (SELECT review_id, COUNT(*) AS likes_count FROM useful WHERE is_like = TRUE " +
                "GROUP BY review_id) likes ON likes.review_id = r.review_id " +
                "LEFT JOIN (SELECT review_id, COUNT(*) AS dislikes_count FROM useful WHERE is_like = FALSE " +
                "GROUP BY review_id) dislikes ON dislikes.review_id = r.review_id " +
                "WHERE r.film_id = ? " +
                "ORDER BY (likes_count - dislikes_count) DESC, likes_count DESC  " +
                "LIMIT ?";
        List<Review> reviews = jdbc.query(reviewByFilm, reviewMapper, filmId, count);
        log.info("Получены отзывы о фильме id = {} {}. Количество отзывов {}.", filmId, reviews, reviews.size());
        return reviews;
    }

    public List<Review> getAllReviews(int count) {
        final String review = "SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, likes.likes_count, " +
                "dislikes.dislikes_count " +
                "FROM reviews r " +
                "LEFT JOIN (SELECT review_id, COUNT(*) AS likes_count FROM useful WHERE is_like = TRUE " +
                "GROUP BY review_id) likes " +
                "ON likes.review_id = r.review_id " +
                "LEFT JOIN (SELECT review_id, COUNT(*) AS dislikes_count FROM useful WHERE is_like = FALSE " +
                "GROUP BY review_id) dislikes " +
                "ON dislikes.review_id = r.review_id " +
                "ORDER BY (COALESCE(likes.likes_count, 0) - COALESCE(dislikes.dislikes_count, 0)) DESC " +
                "LIMIT ?";
        List<Review> reviews = jdbc.query(review, reviewMapper, count);
        log.info("Получены отзывы (не более {}) о фильмах {}.", count, reviews);
        return reviews;
    }

    public void likeOrDislikeToReview(Long reviewId, Long userId, boolean isLike) {
        String checkSql = "SELECT is_like FROM useful WHERE review_id = ? AND user_id = ?";
        List<Map<String, Object>> likeDislike = jdbc.queryForList(checkSql, reviewId, userId);

        if (!likeDislike.isEmpty()) {
            String updateSql = "UPDATE useful SET is_like = ? WHERE review_id = ? AND user_id = ?";
            jdbc.update(updateSql, isLike, reviewId, userId);
            log.info("Пользователь id={} изменил {} на {} отзыву c id={}.", userId, !isLike ? "лайк" : "дизлайк", isLike ? "лайк" : "дизлайк", reviewId);
        } else {
            String insertSql = "INSERT INTO useful (review_id, user_id, is_like) VALUES (?, ?, ?)";
            jdbc.update(insertSql, reviewId, userId, isLike);
            log.info("Пользователь id={} поставил {} отзыву c id={}", userId, isLike ? "лайк" : "дизлайк", reviewId);
        }
    }

    public void deleteLikeOrDislike(Long reviewId, Long userId, boolean isLike) {
        String action = isLike ? "Лайк" : "Дизлайк";
        final String sql = "DELETE FROM useful WHERE review_id = ? AND user_id = ?";
        jdbc.update(sql, reviewId, userId);
        log.info(action + " удален.");
    }
}