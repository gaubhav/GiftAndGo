package com.giftAndGo.assignment.domain.exception;

public class InputProcessingException extends RuntimeException {

    public InputProcessingException(String message) {
        super(message);
    }
    public InputProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
