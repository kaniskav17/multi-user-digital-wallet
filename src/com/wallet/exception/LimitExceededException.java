package com.wallet.exception;

public class LimitExceededException extends WalletException {
    public LimitExceededException(String msg) { super(msg); }
}
