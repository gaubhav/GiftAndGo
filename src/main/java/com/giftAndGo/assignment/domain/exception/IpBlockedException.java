package com.giftAndGo.assignment.domain.exception;

public class IpBlockedException extends RuntimeException{
    public IpBlockedException(String message) {
        super(message);
    }
}
