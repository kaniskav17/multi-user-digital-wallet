package com.wallet.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Abstract base of all user categories; subclasses supply limits, privileges and cashback. */
public abstract class User {
    private final String id;
    private final String name;
    private final Wallet wallet;
    private BigDecimal spentToday = BigDecimal.ZERO;
    private LocalDate spendDate = LocalDate.now();

    protected User(String id, String name) {
        this.id = id; this.name = name; this.wallet = new Wallet(id);
    }

    public abstract String getCategory();
    public abstract BigDecimal getPerTransactionLimit();
    public abstract BigDecimal getDailyLimit();
    public abstract BigDecimal getMaxBalance();
    public abstract boolean canWithdrawToBank();
    public abstract CashbackPolicy getCashbackPolicy();

    public String getId() { return id; }
    public String getName() { return name; }
    public Wallet getWallet() { return wallet; }

    /** Amount already spent today (resets automatically when the date changes). Call under wallet lock. */
    public BigDecimal getSpentToday() {
        if (!spendDate.equals(LocalDate.now())) { spentToday = BigDecimal.ZERO; spendDate = LocalDate.now(); }
        return spentToday;
    }
    public void addSpent(BigDecimal amt) { spentToday = getSpentToday().add(amt); }

    @Override public String toString() { return name + " (" + id + ", " + getCategory() + ")"; }
}
