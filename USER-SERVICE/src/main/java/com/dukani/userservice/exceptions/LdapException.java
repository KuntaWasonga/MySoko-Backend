package com.dukani.userservice.exceptions;

public class LdapException extends RuntimeException {
    public LdapException(String message, Throwable cause) {
        super(message, cause);
    }
}
