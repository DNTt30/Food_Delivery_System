package com.duong.salesmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user tries to access a chat / contact-info
 * for an order they do not belong to.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ChatAccessDeniedException extends RuntimeException {

    public ChatAccessDeniedException(String message) {
        super(message);
    }
}
