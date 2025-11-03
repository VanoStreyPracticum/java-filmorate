package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDTO;
import ru.yandex.practicum.filmorate.model.Mpa;

@Component
public class MpaMapper {

    public MpaDTO toDTO(Mpa mpa) {
        return new MpaDTO(mpa.getId(), mpa.getName());
    }
}
