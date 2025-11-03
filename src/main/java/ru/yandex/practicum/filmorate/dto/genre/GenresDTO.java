package ru.yandex.practicum.filmorate.dto.genre;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GenresDTO {
    private Integer id;
    private String name;
}
