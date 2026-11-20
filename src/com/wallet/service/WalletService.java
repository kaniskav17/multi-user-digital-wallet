package com.wallet.service;

import com.wallet.exception.*;
import com.wallet.model.*;
import com.wallet.storage.TransactionLogger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class WalletService {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final List<Transaction> ledger = new CopyOnWriteArrayList<>();
    private final AtomicLong counter = new AtomicLong(1000);
    private final TransactionLogger logger;

    public WalletService(TransactionLogger logger) { this.logger = logger; }

    public void register(User u) { users.put(u.getId(), u); }

    public User getUser(String id) throws UserNotFoundException {
        User u = users.get(id);
        if (u == null) throw new UserNotFoundException(id);
        return u;
    }

    private String nextId() { return "TXN" + counter.incrementAndGet(); }

    private BigDecimal money(BigDecimal a) { return a.setScale(2, RoundingMode.HALF_UP); }

    /** Add money to a wallet. */
    public void topUp(String userId, BigDecimal amount) throws WalletException {
        User u = getUser(userId);
        Wallet w = u.getWallet();
        w.lock();
        try {
            amount = money(amount);
            if (amount.signum() <= 0) throw new InvalidAmountException("Amount must be positive");
            if (w.getBalance().add(amount).compareTo(u.getMaxBalance()) > 0)
                throw new LimitExceededException("Wallet balance limit exceeded for " + u.getCategory());
            w.credit(amount);
            record(new Transaction(nextId(), TransactionType.TOP_UP, "BANK", userId, amount,
                    TransactionStatus.SUCCESS, "Top-up"), w);
        } finally { w.unlock(); }
    }

    /** Overloaded convenience version without a remark. */
    public Transaction transfer(String fromId, String toId, BigDecimal amount) throws WalletException {
        return transfer(fromId, toId, amount, "Transfer");
    }

    /** Thread-safe transfer: locks both wallets in a fixed order (by id) to prevent deadlock. */
    public Transaction transfer(String fromId, String toId, BigDecimal amount, String remark)
            throws WalletException {
        User from = getUser(fromId);
        User to = getUser(toId);
        Wallet a = from.getWallet(), b = to.getWallet();
        Wallet first = a.getId().compareTo(b.getId()) <= 0 ? a : b;
        Wallet second = (first == a) ? b : a;
        BigDecimal amt = money(amount);

        first.lock();
        try {
            if (second != first) second.lock();
            try {
                validate(from, to, amt);
                BigDecimal cashback = from.getCashbackPolicy().calculate(amt);
                a.debit(amt);
                b.credit(amt);
                from.addSpent(amt);
                Transaction t = new Transaction(nextId(), TransactionType.TRANSFER, fromId, toId,
                        amt, TransactionStatus.SUCCESS, remark);
                record(t, a);
                b.addHistory(t);
                if (cashback.signum() > 0) {
                    a.credit(cashback);
                    record(new Transaction(nextId(), TransactionType.CASHBACK, "SYSTEM", fromId,
                            cashback, TransactionStatus.SUCCESS, "Cashback on " + t.getId()), a);
                }
                return t;
            } catch (WalletException e) {
                Transaction f = new Transaction(nextId(), TransactionType.TRANSFER, fromId, toId,
                        amt, TransactionStatus.FAILED, e.getMessage());
                ledger.add(f);
                logger.log(f);
                throw e;
            } finally { if (second != first) second.unlock(); }
        } finally { first.unlock(); }
    }

    private void validate(User from, User to, BigDecimal amt) throws WalletException {
        if (amt.signum() <= 0) throw new InvalidAmountException("Amount must be positive");
        if (from == to) throw new InvalidAmountException("Cannot transfer to yourself");
        if (amt.compareTo(from.getPerTransactionLimit()) > 0)
            throw new LimitExceededException("Per-transaction limit of Rs." + from.getPerTransactionLimit()
                    + " exceeded for " + from.getCategory());
        if (from.getSpentToday().add(amt).compareTo(from.getDailyLimit()) > 0)
            throw new LimitExceededException("Daily limit of Rs." + from.getDailyLimit() + " exceeded");
        if (from.getWallet().getBalance().compareTo(amt) < 0)
            throw new InsufficientBalanceException("Insufficient balance");
        if (to.getWallet().getBalance().add(amt).compareTo(to.getMaxBalance()) > 0)
            throw new LimitExceededException("Receiver's wallet limit exceeded");
    }

    private void record(Transaction t, Wallet w) {
        w.addHistory(t);
        ledger.add(t);
        logger.log(t);
    }

    public List<Transaction> getLedger() { return List.copyOf(ledger); }
}
