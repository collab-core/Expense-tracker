package org.example.expense_tracker.pattern.adapter;

import java.math.BigDecimal;

public interface CurrencyConverter {
    BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency);
}
