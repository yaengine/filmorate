package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class User {
    Long id;
    String name;
    String email;
    String login;
    LocalDate birthday;
}
