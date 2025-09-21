package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final ValidationService validationService;

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
        return userStorage.getUser(id).orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    public void addFriend(long id, long friendId) {
        User user = getById(id);
        User friend = getById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(id);
    }

    public void removeFriend(long id, long friendId) {
        User user = getById(id);
        User friend = getById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
    }

    public List<User> getFriends(long id) {
        User user = getById(id);
        return user.getFriends().stream()
                .map(userStorage::getUser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long id, long otherId) {
        User user = getById(id);
        User other = getById(otherId);
        Set<Long> common = new HashSet<>(user.getFriends());
        common.retainAll(other.getFriends());
        return common.stream()
                .map(userStorage::getUser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}