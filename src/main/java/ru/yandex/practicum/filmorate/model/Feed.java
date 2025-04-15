package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

@Data
@Builder
public class Feed {
    Long eventId;
    Long userId;
    Long entityId;
    Long timestamp;
    EventType eventType;
    Operation operation;

}
