package com.visasim.userservice.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visasim.userservice.dto.CreateUserRequest;
import com.visasim.userservice.exceptions.UserHasDependentDataException;
import com.visasim.userservice.exceptions.UserNotFoundException;
import com.visasim.userservice.model.User;
import com.visasim.userservice.repository.UserRepository;
import com.visasim.userservice.repository.WalletRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public UserService(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        User user = new User(request.fullName(), request.email());
        return userRepository.save(user);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public void deleteUser(UUID userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

    if (walletRepository.findByUserId(userId).isPresent()) {
        throw new UserHasDependentDataException(userId);
    }

    userRepository.delete(user);
}
}