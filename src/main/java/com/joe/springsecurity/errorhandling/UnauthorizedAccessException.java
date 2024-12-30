package com.joe.springsecurity.errorhandling;

public class UnauthorizedAccessException extends RuntimeException  {

    // Constructor that accepts a message
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    // Constructor that accepts a message and a cause
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }

}
