package com.kahanchale.splitexpense.exception;

public class InvalidSplitException extends RuntimeException {

    public InvalidSplitException(String message) {
        super(message);
    }

    public InvalidSplitException(String message, Throwable cause) {
        super(message, cause);
    }
}