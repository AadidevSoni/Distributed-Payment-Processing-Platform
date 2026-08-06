package com.visasim.userservice.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.visasim.userservice.dto.CreateUserRequest;
import com.visasim.userservice.exceptions.UserNotFoundException;
import com.visasim.userservice.model.User;

@Service
public class UserService {

    private final Map<UUID, User> userStore = new ConcurrentHashMap<>();

    public User createUser(CreateUserRequest request) {
        User user = new User(request.fullName(), request.email());
        userStore.put(user.getId(), user);
        return user;
    }

    public User getUserById(UUID id) {
        User user = userStore.get(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return user;
    }
}