package com.visasim.userservice.exceptions;

import java.util.UUID;

public class UserHasDependentDataException extends RuntimeException {
    public UserHasDependentDataException(UUID userId) {
        super("Cannot delete user " + userId + ": user has an existing wallet. Delete the wallet first.");
    }
}