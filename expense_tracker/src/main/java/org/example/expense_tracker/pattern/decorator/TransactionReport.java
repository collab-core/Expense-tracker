package org.example.expense_tracker.pattern.decorator;

import org.example.expense_tracker.model.Transaction;
import java.util.List;

public interface TransactionReport {
    double calculateTotal(List<Transaction> transactions);
}
