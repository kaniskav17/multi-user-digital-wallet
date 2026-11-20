package com.wallet.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PremiumUser extends User {
    public PremiumUser(String id, String name) { super(id, name); }

    @Override public String getCategory() { return "PREMIUM"; }
    @Override public BigDecimal getPerTransactionLimit() { return new BigDecimal("25000"); }
    @Override public BigDecimal getDailyLimit() { return new BigDecimal("100000"); }
    @Override public BigDecimal getMaxBalance() { return new BigDecimal("200000"); }
    @Override public boolean canWithdrawToBank() { return true; }

    /** Tiered: 2% up to Rs.10,000, 3% above that, capped at Rs.500. */
    @Override public CashbackPolicy getCashbackPolicy() {
        return amt -> {
            BigDecimal rate = amt.compareTo(new BigDecimal("10000")) <= 0
                    ? new BigDecimal("0.02") : new BigDecimal("0.03");
            return amt.multiply(rate).setScale(2, RoundingMode.HALF_UP).min(new BigDecimal("500"));
        };
    }
}
