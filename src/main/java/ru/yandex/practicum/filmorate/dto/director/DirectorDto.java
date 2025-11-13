package ru.yandex.practicum.filmorate.dto.director;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectorDto {
    private Integer id;
    private String name;
}
