package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

/**
 * Режиссер.
 */
@Data
@Builder
public class Director {
    Long id;
    String name;
}
