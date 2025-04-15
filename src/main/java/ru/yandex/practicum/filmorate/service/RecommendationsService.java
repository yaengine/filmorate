package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationsService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public RecommendationsService(@Qualifier("userDbStorage") UserStorage userStorage,
                                 @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Collection<Film> findRecomendationByUserId(@PathVariable long userId) {
        //List<Film> films = (List<Film>) filmStorage.findAll();
        Collection<User> users = userStorage.findAll();
        //ID фильмов, которые лайкнул пользователь
        Set<Long> userFilmIds = filmStorage.getLikedFilms(userId).stream()
                .map(Film::getId)
                .collect(Collectors.toSet());
        //Максимально похожий пользователь по лайкам
        User similarLikesUser;

        Map<User, Set<Long>> userLikes = new HashMap<>();
        //Заполняем данные с лайками пользователей
        for (User user : users) {
            if (userId == user.getId()) {
                continue;
            }
            userLikes.put(user, filmStorage.getLikedFilms(user.getId()).stream()
                            .map(Film::getId).collect(Collectors.toSet()));
        }

        //Ищем пересечение по лайкам
        Map<User, Long> userSimilarity = users.stream()
                .filter(user -> user.getId() != userId)
                .collect(Collectors.toMap(
                        user -> user,
                        user -> {
                            Set<Long> otherUserFilmIds = filmStorage.getLikedFilms(user.getId()).stream()
                                    .map(Film::getId)
                                    .collect(Collectors.toSet());
                            return userFilmIds.stream()
                                    .filter(otherUserFilmIds::contains)
                                    .count();
                        }
                ));
        // Найдем максимально похожего по лайкам
        Optional<User> optSimilarLikesUser = userSimilarity.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<User, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .findFirst();

        if (optSimilarLikesUser.isEmpty()) {
            return Collections.emptyList();
        } else {
            similarLikesUser = optSimilarLikesUser.get();
        }

        //Ищем ID фильмов, которые лайкнули похожий пользователь
        Set<Long> similarUserLikes = userLikes.get(similarLikesUser);

        // Находим разницу - ID фильмов, которые не лайкнул наш, но лайкнул похожий пользователь
        Set<Long> diff = new HashSet<>(similarUserLikes);
        diff.removeAll(userFilmIds);

        return diff.stream()
                .map(filmStorage::findFilmById)
                .collect(Collectors.toCollection(HashSet::new));
    }
}

