package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    Director create(Director director);

    Director update(Director director);

    Optional<Director> getById(int id);

    boolean existsById(int id);

    List<Director> getAll();

    void delete(int id);
}
