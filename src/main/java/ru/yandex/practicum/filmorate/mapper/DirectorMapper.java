package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;

@Component
public class DirectorMapper {

    public DirectorDto toDto(Director director) {
        if (director == null) return null;
        return new DirectorDto(director.getId(), director.getName());
    }

    public Director toModel(DirectorDto dto) {
        if (dto == null) return null;
        return new Director(dto.getId(), dto.getName());
    }
}
