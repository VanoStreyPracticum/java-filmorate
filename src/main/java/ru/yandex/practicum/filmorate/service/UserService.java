package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.util.EventType;
import ru.yandex.practicum.filmorate.util.OperationType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final ValidationService validationService;
    private final EventService eventService;

    public User create(User user) {
        log.info("Создание пользователя с логином: {}", user.getLogin());
        validationService.validateNewUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.addUser(user);
    }

    public User update(User user) {
        log.info("Обновление пользователя с ID: {}", user.getId());
        if (user.getId() == null) {
            throw new ValidationException("ID обязателен для обновления пользователя");
        }
        validationService.validateNewUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.updateUser(user);
    }

    public Collection<User> getAll() {
        log.info("Получение всех пользователей");
        return userStorage.getAllUsers();
    }

    public User getById(long id) {
        log.info("Получение пользователя с ID: {}", id);
        return userStorage.getUser(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    public boolean deleteUser(long id) {
        log.info("Удаление пользователя с ID: {}", id);
        return userStorage.deleteUser(id);
    }

    public void addFriend(long userId, long friendId) {
        log.info("Добавление друга: пользователь {} добавляет {}", userId, friendId);
        if (!userStorage.existsUser(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        if (!userStorage.existsUser(friendId)) {
            throw new NotFoundException("Пользователь не найден: " + friendId);
        }
        userStorage.addFriend(userId, friendId);
        eventService.createEvent(userId, EventType.FRIEND.name(), OperationType.ADD.name(), friendId);
    }

    public void removeFriend(long userId, long friendId) {
        log.info("Удаление друга: пользователь {} удаляет {}", userId, friendId);
        if (!userStorage.existsUser(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        if (!userStorage.existsUser(friendId)) {
            throw new NotFoundException("Пользователь не найден: " + friendId);
        }
        userStorage.deleteFriend(userId, friendId);
        eventService.createEvent(userId, EventType.FRIEND.name(), OperationType.REMOVE.name(), friendId);
    }

    public List<User> getFriends(long userId) {
        log.info("Получение друзей пользователя с ID: {}", userId);
        if (!userStorage.existsUser(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        return new ArrayList<>(userStorage.getFriendsList(userId));
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        log.info("Получение общих друзей для пользователей {} и {}", userId, otherId);
        if (!userStorage.existsUser(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        if (!userStorage.existsUser(otherId)) {
            throw new NotFoundException("Пользователь не найден: " + otherId);
        }
        return new ArrayList<>(userStorage.getCommonFriends(userId, otherId));
    }
}
