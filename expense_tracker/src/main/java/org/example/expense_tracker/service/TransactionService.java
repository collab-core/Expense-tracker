package org.example.expense_tracker.service;

import org.example.expense_tracker.model.Category;
import org.example.expense_tracker.model.Transaction;
import org.example.expense_tracker.model.TransactionFactory;
import org.example.expense_tracker.model.TransactionType;
import org.example.expense_tracker.model.User;
import org.example.expense_tracker.pattern.dao.TransactionDAO;
import org.example.expense_tracker.pattern.observer.TransactionObserver;
import org.example.expense_tracker.pattern.observer.TransactionSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService implements ITransactionService, TransactionSubject {

    private final TransactionDAO transactionDAO;
    private final UserSession userSession;
    private final List<TransactionObserver> observers = new ArrayList<>();

    @Autowired
    public TransactionService(TransactionDAO transactionDAO, UserSession userSession) {
        this.transactionDAO = transactionDAO;
        this.userSession = userSession;
    }

    @Override
    public void addObserver(TransactionObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(TransactionObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (TransactionObserver observer : observers) {
            observer.onTransactionUpdated();
        }
    }

    @Override
    public Transaction saveTransaction(BigDecimal amount, TransactionType type, Category category, LocalDate date, String description) {
        User user = userSession.getLoggedInUser();
        // Fallback check, although Proxy will handle this first.
        if (user == null) {
            throw new IllegalStateException("No user logged in");
        }
        
        // Using Factory Pattern
        Transaction transaction = TransactionFactory.createTransaction(type, amount, description, date, category, user);

        // Using DAO Pattern
        Transaction savedTransaction = transactionDAO.save(transaction);
        
        // Using Observer Pattern
        notifyObservers();
        
        return savedTransaction;
    }

    // Used by Command Pattern
    public void saveDirectTransaction(Transaction transaction) {
        transactionDAO.save(transaction);
        notifyObservers();
    }

    // Used by Command Pattern
    public void deleteSpecificTransaction(Transaction transaction) {
        transactionDAO.delete(transaction);
        notifyObservers();
    }

    private List<Transaction> getTransactionsForCurrentUser() {
        User user = userSession.getLoggedInUser();
        if (user == null) {
            return List.of();
        }
        return transactionDAO.findByUser(user);
    }

    public Map<Category, Double> getExpenseBreakdown() {
        return getTransactionsForCurrentUser().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(t -> t.getAmount().doubleValue())
                ));
    }

    public Map<LocalDate, Double> getDailySpending() {
        return getTransactionsForCurrentUser().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        Transaction::getDate,
                        Collectors.summingDouble(t -> t.getAmount().doubleValue())
                ));
    }

    public Map<LocalDate, Double> getExpenseTrend() {
        return getTransactionsForCurrentUser().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        Transaction::getDate,
                        Collectors.summingDouble(t -> t.getAmount().doubleValue())
                ));
    }

    public Map<LocalDate, Double> getIncomeTrend() {
        return getTransactionsForCurrentUser().stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .collect(Collectors.groupingBy(
                        Transaction::getDate,
                        Collectors.summingDouble(t -> t.getAmount().doubleValue())
                ));
    }
}