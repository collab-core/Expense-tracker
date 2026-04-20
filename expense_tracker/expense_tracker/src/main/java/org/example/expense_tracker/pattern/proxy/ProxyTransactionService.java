package org.example.expense_tracker.pattern.proxy;

import org.example.expense_tracker.model.Category;
import org.example.expense_tracker.model.Transaction;
import org.example.expense_tracker.model.TransactionType;
import org.example.expense_tracker.model.User;
import org.example.expense_tracker.service.ITransactionService;
import org.example.expense_tracker.service.TransactionService;
import org.example.expense_tracker.service.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Primary
public class ProxyTransactionService implements ITransactionService {

    private final TransactionService transactionService;
    private final UserSession userSession;

    @Autowired
    public ProxyTransactionService(TransactionService transactionService, UserSession userSession) {
        this.transactionService = transactionService;
        this.userSession = userSession;
    }

    @Override
    public Transaction saveTransaction(BigDecimal amount, TransactionType type, Category category, LocalDate date, String description) {
        User user = userSession.getLoggedInUser();
        if (user == null) {
            throw new SecurityException("Access Denied: Only logged-in users can add expenses.");
        }
        return transactionService.saveTransaction(amount, type, category, date, description);
    }
    
    @Override
    public java.util.Map<Category, Double> getExpenseBreakdown() {
        return transactionService.getExpenseBreakdown();
    }

    @Override
    public java.util.Map<LocalDate, Double> getDailySpending() {
        return transactionService.getDailySpending();
    }

    @Override
    public java.util.Map<LocalDate, Double> getExpenseTrend() {
        return transactionService.getExpenseTrend();
    }

    @Override
    public java.util.Map<LocalDate, Double> getIncomeTrend() {
        return transactionService.getIncomeTrend();
    }
}
