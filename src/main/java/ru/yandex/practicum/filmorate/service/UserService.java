package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    @Autowired
    private final UserStorage userStorage;

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public void addFriend(long userId, long friendId) {
        userStorage.findUserById(userId).getFriends().add(friendId);
        userStorage.findUserById(friendId).getFriends().add(userId);
    }

    public void removeFriend(long userId, long friendId) {
        User user = userStorage.findUserById(userId);
        User friend = userStorage.findUserById(friendId);
        if (user.getFriends().contains(friend.getId()) &&
                friend.getFriends().contains(userId)) {
            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
        } else {
            //throw new ValidationException("Пользователи с id = " + userId + " и с id = " + friendId + " не дружат");
            log.info("Пользователи с id = " + userId + " и с id = " + friendId + " не дружат");
        }
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
}
