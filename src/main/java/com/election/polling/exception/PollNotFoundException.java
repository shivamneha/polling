package com.election.polling.exception;

public class PollNotFoundException extends RuntimeException {

    public PollNotFoundException(String message) {
        super(message);
    }
}
