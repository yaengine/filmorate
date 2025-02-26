package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    UserController userController;
    UserService userService;
    InMemoryUserStorage inMemoryUserStorage;
    User user;

    @BeforeEach
    void beforeEach() {
        inMemoryUserStorage = new InMemoryUserStorage();
        userService = new UserService(inMemoryUserStorage);
        userController = new UserController(userService);
        user = User.builder()
                .id(1L)
                .name("UserName")
                .email("user@email.com")
                .birthday(LocalDate.parse("2001-10-05"))
                .login("UserLogin")
                .build();
    }

    @Test
    void create() {
        User usr = userController.create(user);
        assertEquals(user, usr, "Пользователь не создался");
    }

    @Test
    void createWithEmptyEmail() {
        User usr = userController.create(user);
        usr.setEmail("");
        assertThrows(ValidationException.class, () -> userController.update(usr), "Создание пользователя с  " +
                "пустым полем email должно приводить к исключению");
    }

    @Test
    void createWithEmptyLogin() {
        User usr = userController.create(user);
        usr.setLogin("");
        assertThrows(ValidationException.class, () -> userController.update(usr), "Создание пользователя с пустым " +
                "полем login должно приводить к исключению");
    }

    @Test
    void createWithBirthdayInFuture() {
        User usr = userController.create(user);
        usr.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> userController.update(usr), "Создание пользователя " +
                "полем с днем рождения из будущего должно приводить к исключению");
    }

    @Test
    void createWithEmptyName() {
        user.setName("");
        User usr = userController.create(user);
        assertEquals(usr.getName(), user.getLogin(), "При создании пользователя с " +
                "пустым полем name, оно должно заполняться из поля login");
    }

    @Test
    void update() {
        User usr = userController.create(user);
        User usr1 = userController.update(usr);
        assertEquals(usr, usr1, "Пользователь не обновился");
    }

    @Test
    void updateWithEmptyEmail() {
        User usr = userController.update(userController.create(user));
        usr.setEmail("");
        assertThrows(ValidationException.class, () -> userController.update(usr), "Обновление пользователя с  " +
                "пустым полем email должно приводить к исключению");
    }

    @Test
    void updateWithEmptyLogin() {
        User usr = userController.update(userController.create(user));
        usr.setLogin("");
        assertThrows(ValidationException.class, () -> userController.update(usr), "Обновление пользователя с пустым " +
                "полем login должно приводить к исключению");
    }

    @Test
    void updateWithBirthdayInFuture() {
        User usr = userController.update(userController.create(user));
        usr.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> userController.update(usr), "Обновление пользователя " +
                "полем с днем рождения из будущего должно приводить к исключению");
    }

    @Test
    void updateWithEmptyName() {
        User usr = userController.create(user);
        usr.setName("");
        User usr1 = userController.update(usr);
        assertEquals(usr1.getName(), usr.getLogin(), "При обновлении пользователя с " +
                "пустым полем name, оно должно заполняться из поля login");
    }

    @Test
    void findAll() {
        User usr = userController.create(user);
        assertTrue(userController.findAll().contains(usr), "Пользователь не найден");
    }
}
