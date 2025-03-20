package ru.yandex.practicum.filmorate.storage.user.UserStorage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    public Collection<User> findAll();

    public User create(User user);

    public User update(User newUser);

    public User findUserById(long id);

    public void addFriend(long userId, long friendId);
}
