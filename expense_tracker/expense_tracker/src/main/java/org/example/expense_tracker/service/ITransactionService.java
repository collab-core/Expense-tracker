package org.example.expense_tracker.service;

import org.example.expense_tracker.model.Category;
import org.example.expense_tracker.model.Transaction;
import org.example.expense_tracker.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ITransactionService {
    Transaction saveTransaction(BigDecimal amount, TransactionType type, Category category, LocalDate date, String description);
    java.util.Map<Category, Double> getExpenseBreakdown();
    java.util.Map<LocalDate, Double> getDailySpending();
    java.util.Map<LocalDate, Double> getExpenseTrend();
    java.util.Map<LocalDate, Double> getIncomeTrend();
}
