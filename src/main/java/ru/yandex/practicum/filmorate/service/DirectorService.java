package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.NewDirector;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirector;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public DirectorDto create(NewDirector newDirector) {
        log.info("Создание нового режисера {}", newDirector);
        if (newDirector.getName() == null || newDirector.getName().isBlank()) {
            log.error("Невалидное имя директора {}", newDirector.getName());
            throw new ValidationException("Director name is empty");
        }
        Director director = DirectorMapper.toModel(newDirector);
        director = directorStorage.create(director);
        log.info("Создали нового режисера в бд {}", director);
        return DirectorMapper.toDto(director);
    }

    public DirectorDto update(UpdateDirector updateDirector) {
        log.info("Попытка обновления данных режисера {}", updateDirector);
        Optional<Director> optionalDirector = directorStorage.getById(updateDirector.getId());
        if (optionalDirector.isEmpty()) {
            log.error("В базе не найден режисер с ID {}", updateDirector.getId());
            throw new NotFoundException("Режиссёр с id=" + updateDirector.getId() + " не найден");
        }
        Director director = optionalDirector.get();
        director = DirectorMapper.toModel(director, updateDirector);
        directorStorage.update(director);
        log.info("В базе обновлен режисер {}", director);
        return DirectorMapper.toDto(director);
    }

    public DirectorDto getById(Long id) {
        log.info("Получение режисера по ID {}", id);
        Optional<Director> optionalDirector = directorStorage.getById(id);
        if (optionalDirector.isEmpty()) {
            log.error("В базе не найден режисер по ID {}", id);
            throw new NotFoundException("Режиссёр с id=" + id + " не найден");
        }
        Director director = optionalDirector.get();
        log.info("Получен режисер {}", director);
        return DirectorMapper.toDto(director);
    }


    public List<DirectorDto> getAll() {
        log.info("Получение списка всех режисеров");
        List<Director> directorList = directorStorage.getAll();
        log.info("Список режисеров получен {}", directorList);
        return directorList.stream()
                .map(DirectorMapper::toDto)
                .toList();
    }

    public void delete(Long id) {
        log.info("Удаление режисера с ID {}", id);
        directorStorage.delete(id);
        log.info("Режисер удален по ID {}", id);
    }
}
