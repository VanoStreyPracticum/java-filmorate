package ru.yandex.practicum.filmorate.dto.director;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDirector {
    private Long id;
    private String name;

    public boolean hasName(){
        return name != null && !name.isEmpty() && !name.isBlank();
    }
}
