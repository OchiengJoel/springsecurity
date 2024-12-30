package com.joe.springsecurity.errorhandling;

public class ResourceNotFoundException extends RuntimeException{

    // Constructor with a message
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Constructor with a message and cause
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
