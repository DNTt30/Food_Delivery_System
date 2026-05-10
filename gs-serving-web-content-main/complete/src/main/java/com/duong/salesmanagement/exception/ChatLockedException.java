package com.duong.salesmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user tries to send a message to a chat room
 * that is locked (order COMPLETED or CANCELLED).
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ChatLockedException extends RuntimeException {

    public ChatLockedException(String message) {
        super(message);
    }
}
