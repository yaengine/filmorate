package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    UserController UserController = new UserController();
    User user;

    @BeforeEach
    void beforeEach() {
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
        User usr = UserController.create(user);
        assertEquals(user, usr, "Пользователь не создался");
    }

    @Test
    void createWithEmptyEmail() {
        User usr = UserController.create(user);
        usr.setEmail("");
        assertThrows(ValidationException.class, () -> UserController.update(usr), "Создание пользователя с  " +
                "пустым полем email должно приводить к исключению");
    }

    @Test
    void createWithEmptyLogin() {
        User usr = UserController.create(user);
        usr.setLogin("");
        assertThrows(ValidationException.class, () -> UserController.update(usr), "Создание пользователя с пустым " +
                "полем login должно приводить к исключению");
    }

    @Test
    void createWithBirthdayInFuture() {
        User usr = UserController.create(user);
        usr.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> UserController.update(usr), "Создание пользователя " +
                "полем с днем рождения из будущего должно приводить к исключению");
    }

    @Test
    void createWithEmptyName() {
        user.setName("");
        User usr = UserController.create(user);
        assertEquals(usr.getName(), user.getLogin(), "При создании пользователя с " +
                "пустым полем name, оно должно заполняться из поля login");
    }

    @Test
    void update() {
        User usr = UserController.create(user);
        User usr1 = UserController.update(usr);
        assertEquals(usr, usr1, "Пользователь не обновился");
    }

    @Test
    void updateWithEmptyEmail() {
        User usr = UserController.update(UserController.create(user));
        usr.setEmail("");
        assertThrows(ValidationException.class, () -> UserController.update(usr), "Обновление пользователя с  " +
                "пустым полем email должно приводить к исключению");
    }

    @Test
    void updateWithEmptyLogin() {
        User usr = UserController.update(UserController.create(user));
        usr.setLogin("");
        assertThrows(ValidationException.class, () -> UserController.update(usr), "Обновление пользователя с пустым " +
                "полем login должно приводить к исключению");
    }

    @Test
    void updateWithBirthdayInFuture() {
        User usr = UserController.update(UserController.create(user));
        usr.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> UserController.update(usr), "Обновление пользователя " +
                "полем с днем рождения из будущего должно приводить к исключению");
    }

    @Test
    void updateWithEmptyName() {
        User usr = UserController.create(user);
        usr.setName("");
        User usr1 = UserController.update(usr);
        assertEquals(usr1.getName(), usr.getLogin(), "При обновлении пользователя с " +
                "пустым полем name, оно должно заполняться из поля login");
    }

    @Test
    void findAll() {
        User usr = UserController.create(user);
        assertTrue(UserController.findAll().contains(usr), "Пользователь не найден");
    }
}
