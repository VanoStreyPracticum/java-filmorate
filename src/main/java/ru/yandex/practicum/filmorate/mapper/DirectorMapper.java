package ru.yandex.practicum.filmorate.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.NewDirector;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirector;
import ru.yandex.practicum.filmorate.model.Director;

@UtilityClass
public class DirectorMapper {

    public static DirectorDto toDto(Director director) {
        if (director == null) return null;
        return new DirectorDto(director.getId(), director.getName());
    }

    public static Director toModel(NewDirector newDirector) {
        if (newDirector == null) return null;
        return new Director(null, newDirector.getName());
    }

    public static Director toModel(Director director, UpdateDirector updateDirector) {
        if (updateDirector == null) return director;
        if (updateDirector.hasName()) {
            director.setName(updateDirector.getName());
        }
        return director;
    }
}
