package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FeedDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private static final String EMPTY_EMAIL_ERR = "Email не может быть пустой и должен содержать символ @";
    private static final String EMPTY_LOGIN_ERR = "Логин не может быть пустым и содержать пробелы";
    private static final String BIRTHDAY_ERR = "Дата рождения не может быть в будущем";
    private static final String DUPL_EMAIL_ERR = "Этот имейл уже используется";
    private static final String ID_ERR = "Id должен быть указан";

    private final UserStorage userStorage;
    private final FeedDbStorage feedDbStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, FeedDbStorage feedDbStorage) {
        this.userStorage = userStorage;
        this.feedDbStorage = feedDbStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error(EMPTY_EMAIL_ERR);
            throw new ValidationException(EMPTY_EMAIL_ERR);
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error(EMPTY_LOGIN_ERR);
            throw new ValidationException(EMPTY_LOGIN_ERR);
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error(BIRTHDAY_ERR);
            throw new ValidationException(BIRTHDAY_ERR);
        }
        if (findAll().stream()
                .map(User::getEmail)
                .anyMatch(userEmail -> userEmail.equals(user.getEmail()))
        ) {
            log.error(DUPL_EMAIL_ERR);
            throw new ValidationException(DUPL_EMAIL_ERR);
        }
        log.trace("Проверки пройдены");

        return userStorage.create(user);
    }

    public User update(User newUser) {
        log.trace("Начинаем обновлять пользователя");
        log.trace(newUser.toString());
        // проверяем необходимые условия
        if (newUser.getId() == null) {
            log.error(ID_ERR);
            throw new ValidationException(ID_ERR);
        }
        if (newUser.getEmail() == null || newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
            log.error(EMPTY_EMAIL_ERR);
            throw new ValidationException(EMPTY_EMAIL_ERR);
        }
        if (newUser.getLogin() == null || newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
            log.error(EMPTY_LOGIN_ERR);
            throw new ValidationException(EMPTY_LOGIN_ERR);
        }
        if (newUser.getBirthday().isAfter(LocalDate.now())) {
            log.error(BIRTHDAY_ERR);
            throw new ValidationException(BIRTHDAY_ERR);
        }

        Map<Long, User> users = findAll().stream().collect(Collectors.toMap(User::getId, u -> u));
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (!newUser.getEmail().equals(oldUser.getEmail())) {
                if (users.values().stream()
                        .filter(u -> !u.getId().equals(newUser.getId()))
                        .map(User::getEmail)
                        .anyMatch(userEmail -> userEmail.equals(newUser.getEmail()))
                ) {
                    log.error(DUPL_EMAIL_ERR);
                    throw new ValidationException(DUPL_EMAIL_ERR);
                }
            }
            log.trace("Проверки пройдены");

            return userStorage.update(newUser);
        } else {
            log.error("Пользователь с id = {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }
    }

    public void addFriend(long userId, long friendId) {
        User user = userStorage.findUserById(userId);
        User friend = userStorage.findUserById(friendId);
        userStorage.addFriend(user.getId(), friend.getId());
    }

    public void removeFriend(long userId, long friendId) {
        User user = userStorage.findUserById(userId);
        User friend = userStorage.findUserById(friendId);
        userStorage.removeFriend(user.getId(), friend.getId());
    }

    public Collection<User> getFriends(long userId) {
        return userStorage.findUserById(userId).getFriends().stream()
                .map(userStorage::findUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(long userId, long friendId) {
        return userStorage.findUserById(userId).getFriends().stream()
                .map(userStorage::findUserById)
                .filter(user -> userStorage.findUserById(friendId).getFriends().contains(user.getId()))
                .collect(Collectors.toList());
    }

    public void deleteUser(long id) {
        userStorage.deleteUser(id);
    }

    public User findUserById(long id) {
        return userStorage.findUserById(id);
    }

    public List<Feed> getUserFeed(Long id) {
        findUserById(id);
        return feedDbStorage.getFeedByUser(id);
    }
}
