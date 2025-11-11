package com.supermarket.ovenupdate.poc.infraestructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class IpAddressInUseException extends RuntimeException {
    public IpAddressInUseException(String message) {
        super(message);
    }
}
