package org.example.expense_tracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionFactory {

    /**
     * Factory method to dynamically create a Transaction object based on the type.
     */
    public static Transaction createTransaction(TransactionType type, BigDecimal amount, String description, LocalDate date, Category category, User user) {
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setDate(date);
        transaction.setCategory(category);
        transaction.setUser(user);
        return transaction;
    }
    
    // Future expansion: Create specific overloaded factory methods for default Expenses or default Incomes
    public static Transaction createExpense(BigDecimal amount, String description, LocalDate date, Category category, User user) {
        return createTransaction(TransactionType.EXPENSE, amount, description, date, category, user);
    }

    public static Transaction createIncome(BigDecimal amount, String description, LocalDate date, Category category, User user) {
        return createTransaction(TransactionType.INCOME, amount, description, date, category, user);
    }
}
