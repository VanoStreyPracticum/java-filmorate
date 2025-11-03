package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.genre.GenresDTO;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GenreMapper {

    public GenresDTO toDTO(Genre genre) {
        return new GenresDTO(genre.getId(), genre.getName());
    }

    public Set<GenresDTO> toDTOSet(Set<Genre> genres) {
        if (genres == null) return null;
        return genres.stream()
                .map(this::toDTO)
                .collect(Collectors.toSet());
    }
    public Genre toEntity(GenresDTO dto) {
        return new Genre(dto.getId(), dto.getName());
    }
}
