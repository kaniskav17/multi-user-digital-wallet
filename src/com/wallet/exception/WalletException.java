package com.wallet.exception;

/** Base checked exception for all wallet errors. */
public class WalletException extends Exception {
    public WalletException(String message) { super(message); }
}
