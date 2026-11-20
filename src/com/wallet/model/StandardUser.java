package com.wallet.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StandardUser extends User {
    public StandardUser(String id, String name) { super(id, name); }

    @Override public String getCategory() { return "STANDARD"; }
    @Override public BigDecimal getPerTransactionLimit() { return new BigDecimal("5000"); }
    @Override public BigDecimal getDailyLimit() { return new BigDecimal("20000"); }
    @Override public BigDecimal getMaxBalance() { return new BigDecimal("50000"); }
    @Override public boolean canWithdrawToBank() { return false; }

    /** Flat 1% cashback, capped at Rs.50 per transaction. */
    @Override public CashbackPolicy getCashbackPolicy() {
        return amt -> amt.multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP)
                         .min(new BigDecimal("50"));
    }
}
