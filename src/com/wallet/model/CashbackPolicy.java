package com.wallet.model;

import java.math.BigDecimal;

/** Strategy interface: each user category plugs in its own cashback rule. */
@FunctionalInterface
public interface CashbackPolicy {
    BigDecimal calculate(BigDecimal amount);
}
