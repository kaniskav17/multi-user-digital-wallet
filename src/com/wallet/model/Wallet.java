package com.wallet.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/** Holds the balance and history of one user. Callers must hold lock() while changing state. */
public class Wallet {
    private final String id;
    private BigDecimal balance = BigDecimal.ZERO;
    private final List<Transaction> history = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public Wallet(String id) { this.id = id; }

    public String getId() { return id; }
    public void lock() { lock.lock(); }
    public void unlock() { lock.unlock(); }

    public BigDecimal getBalance() {
        lock.lock();
        try { return balance; } finally { lock.unlock(); }
    }
    public void credit(BigDecimal amt) { balance = balance.add(amt); }
    public void debit(BigDecimal amt)  { balance = balance.subtract(amt); }
    public void addHistory(Transaction t) { history.add(t); }

    public List<Transaction> getHistory() {
        lock.lock();
        try { return new ArrayList<>(history); } finally { lock.unlock(); }
    }
}
