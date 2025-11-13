package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        if (!directorStorage.existsById(director.getId())) {
            throw new NotFoundException("Режиссёр с id=" + director.getId() + " не найден");
        }
        return directorStorage.update(director);
    }

    public Director getById(int id) {
        return directorStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + id + " не найден"));
    }


    public List<Director> getAll() {
        return directorStorage.getAll();
    }

    public void delete(int id) {
        directorStorage.delete(id);
    }

    public boolean existsById(int id) {
        return directorStorage.existsById(id);
    }
}
