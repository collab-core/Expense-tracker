package org.example.expense_tracker.pattern.decorator;

import org.example.expense_tracker.model.Transaction;
import java.util.List;

public class BaseTransactionReport implements TransactionReport {
    @Override
    public double calculateTotal(List<Transaction> transactions) {
        return transactions.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
    }
}
