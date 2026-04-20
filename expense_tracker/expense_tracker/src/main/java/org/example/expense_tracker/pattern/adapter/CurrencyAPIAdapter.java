package org.example.expense_tracker.pattern.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CurrencyAPIAdapter implements CurrencyConverter {

    private final ThirdPartyCurrencyAPI externalApi;

    @Autowired
    public CurrencyAPIAdapter(ThirdPartyCurrencyAPI externalApi) {
        this.externalApi = externalApi;
    }

    @Override
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        // Adapting the double-based API to our BigDecimal-based system
        double rate = externalApi.fetchConversionRate(fromCurrency, toCurrency);
        return amount.multiply(BigDecimal.valueOf(rate)).setScale(2, RoundingMode.HALF_EVEN);
    }
}
