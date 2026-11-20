package com.wallet.exception;

public class UserNotFoundException extends WalletException {
    public UserNotFoundException(String id) { super("User not found: " + id); }
}
