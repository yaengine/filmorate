package ru.yandex.practicum.filmorate.storage.user.UserStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
@Component
@Qualifier("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        log.trace("Начинаем возвращать всех пользователей");
        return users.values();
    }

    @Override
    public User create(User user) {
        // проверяем выполнение необходимых условий
        log.trace("Начинаем создавать пользователя");
        log.trace(user.toString());

        // формируем дополнительные данные
        User usr = User.builder()
                .id(getNextId())
                .name((user.getName() == null || user.getName().isBlank()) ? user.getLogin() : user.getName())
                .email(user.getEmail())
                .login(user.getLogin())
                .birthday(user.getBirthday())
                .friends(new HashSet<>())
                .build();

        // сохраняем новую публикацию в памяти приложения
        users.put(usr.getId(), usr);

        log.trace("Успешно создали пользователя");
        log.trace(usr.toString());
        return usr;
    }

    @Override
    public User update(User newUser) {
        User oldUser = users.get(newUser.getId());
        oldUser.setEmail(newUser.getEmail());
        oldUser.setName((newUser.getName() == null || newUser.getName().isBlank())
                ? newUser.getLogin() : newUser.getName());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setBirthday(newUser.getBirthday());
        if (newUser.getFriends() != null) {
            oldUser.setFriends(newUser.getFriends());
        }

        log.trace("Успешно обновили пользователя");
        log.trace(oldUser.toString());
        return oldUser;
    }

    @Override
    public User findUserById(long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

    @Override
    public void addFriend(long userId, long friendId) {
        findUserById(userId).getFriends().add(friendId);
        findUserById(friendId).getFriends().add(userId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        User user = findUserById(userId);
        User friend = findUserById(friendId);
        if (user.getFriends().contains(friend.getId()) &&
                friend.getFriends().contains(userId)) {
            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
        } else {
            //throw new ValidationException("Пользователи с id = " + userId + " и с id = " + friendId + " не дружат");
            log.info("Пользователи с id = " + userId + " и с id = " + friendId + " не дружат");
        }
    }

    @Override
    public void deleteUser(long id) {

    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
