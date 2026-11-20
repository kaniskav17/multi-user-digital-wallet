package com.wallet.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Immutable record of one wallet operation. */
public final class Transaction {
    private final String id;
    private final TransactionType type;
    private final String fromUser;
    private final String toUser;
    private final BigDecimal amount;
    private final TransactionStatus status;
    private final String remark;
    private final LocalDateTime time = LocalDateTime.now();

    public Transaction(String id, TransactionType type, String fromUser, String toUser,
                       BigDecimal amount, TransactionStatus status, String remark) {
        this.id = id; this.type = type; this.fromUser = fromUser; this.toUser = toUser;
        this.amount = amount; this.status = status; this.remark = remark;
    }

    public String getId() { return id; }
    public TransactionType getType() { return type; }
    public String getFromUser() { return fromUser; }
    public String getToUser() { return toUser; }
    public BigDecimal getAmount() { return amount; }
    public TransactionStatus getStatus() { return status; }
    public String getRemark() { return remark; }
    public LocalDateTime getTime() { return time; }

    public String toCsv() {
        return String.join(",", id, type.name(), fromUser, toUser, amount.toPlainString(),
                status.name(), time.toString(), remark.replace(',', ';'));
    }

    @Override public String toString() {
        return String.format("%s | %-8s | %s -> %s | Rs.%s | %s | %s",
                id, type, fromUser, toUser, amount, status, remark);
    }
}
