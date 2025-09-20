package ru.yandex.practicum.filmorate.model;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен быть корректным")
    private String email;

    @NotBlank(message = "Login не может быть пустым")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения обязательна")
    private LocalDate birthday;
}
