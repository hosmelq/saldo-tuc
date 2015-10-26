package com.socialimprover.saldotuc.exceptions;

public class InvalidCardException extends IllegalArgumentException {
    public InvalidCardException() {
    }

    public InvalidCardException(String detailMessage) {
        super(detailMessage);
    }
}
