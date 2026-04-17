package org.example.expense_tracker.pattern.adapter;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ThirdPartyCurrencyAPI {
    
    // Simulating an external API that only takes doubles and returns doubles
    public double fetchConversionRate(String source, String target) {
        if (source.equals("USD") && target.equals("EUR")) {
            return 0.93;
        } else if (source.equals("EUR") && target.equals("USD")) {
            return 1.08;
        }
        return 1.0;
    }
}
