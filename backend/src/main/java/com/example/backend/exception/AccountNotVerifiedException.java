package com.example.backend.exception;


public class AccountNotVerifiedException extends RuntimeException {
    public AccountNotVerifiedException(String message) { super(message); }
}