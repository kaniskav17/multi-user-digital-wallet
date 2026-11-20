package com.wallet.model;

import java.math.BigDecimal;

public class MerchantUser extends User {
    public MerchantUser(String id, String name) { super(id, name); }

    @Override public String getCategory() { return "MERCHANT"; }
    @Override public BigDecimal getPerTransactionLimit() { return new BigDecimal("100000"); }
    @Override public BigDecimal getDailyLimit() { return new BigDecimal("500000"); }
    @Override public BigDecimal getMaxBalance() { return new BigDecimal("1000000"); }
    @Override public boolean canWithdrawToBank() { return true; }

    /** Merchants earn no cashback. */
    @Override public CashbackPolicy getCashbackPolicy() { return amt -> BigDecimal.ZERO; }
}
