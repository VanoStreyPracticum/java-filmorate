package ru.yandex.practicum.filmorate.service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ValidationService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final ValidationService validationService;

    public UserService(UserStorage userStorage, ValidationService validationService) {
        this.userStorage = userStorage;
        this.validationService = validationService;
    }

    public User create(User user) {
        validationService.validateNewUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.addUser(user);
    }

    public User update(User user) {
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
        return userStorage.getAllUsers();
    }

    public User getById(long id) {
        return userStorage.getUser(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    public void addFriend(long userId, long friendId) {
        // Проверяем существование обоих пользователей
        if (!userStorage.existsUser(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        if (!userStorage.existsUser(friendId)) {
            throw new NotFoundException("Пользователь не найден: " + friendId);
        }
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        // Проверяем существование обоих пользователей
        if (!userStorage.existsUser(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        if (!userStorage.existsUser(friendId)) {
            throw new NotFoundException("Пользователь не найден: " + friendId);
        }
        userStorage.deleteFriend(userId, friendId);
    }

    public List<User> getFriends(long userId) {
        // Проверяем существование пользователя
        if (!userStorage.existsUser(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        return userStorage.getFriendsList(userId)
                .stream()
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        // Проверяем существование обоих пользователей
        if (!userStorage.existsUser(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
        if (!userStorage.existsUser(otherId)) {
            throw new NotFoundException("Пользователь не найден: " + otherId);
        }
        return userStorage.getCommonFriends(userId, otherId)
                .stream()
                .collect(Collectors.toList());
    }
}
