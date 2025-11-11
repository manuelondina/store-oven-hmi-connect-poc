package com.supermarket.ovenupdate.poc.infraestructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOvenException extends RuntimeException {
    public InvalidOvenException(String message) {
        super(message);
    }
}

