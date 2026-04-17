package org.example.expense_tracker.pattern.decorator;

import org.example.expense_tracker.model.Transaction;
import java.util.List;

public class TaxDecorator extends ReportDecorator {
    private final double taxRate;

    public TaxDecorator(TransactionReport report, double taxRate) {
        super(report);
        this.taxRate = taxRate;
    }

    @Override
    public double calculateTotal(List<Transaction> transactions) {
        double baseTotal = super.calculateTotal(transactions);
        return baseTotal + (baseTotal * taxRate);
    }
}
