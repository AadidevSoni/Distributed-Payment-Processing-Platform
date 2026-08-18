package com.visasim.userservice.exceptions;

public class FraudBlockedException extends RuntimeException {
    public FraudBlockedException(String reasons) {
        super("Transaction blocked by fraud detection: " + reasons);
    }
}