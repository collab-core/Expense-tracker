package org.example.expense_tracker.pattern.decorator;

import java.util.List;

import org.example.expense_tracker.model.Transaction;

public abstract class ReportDecorator implements TransactionReport {
    protected TransactionReport report;

    protected ReportDecorator(TransactionReport report) {
        this.report = report;
    }

    @Override
    public double calculateTotal(List<Transaction> transactions) {
        return report.calculateTotal(transactions);
    }
}
