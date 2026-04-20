package org.example.expense_tracker.pattern.dao;

import org.example.expense_tracker.model.Transaction;
import org.example.expense_tracker.model.User;
import java.util.List;

public interface TransactionDAO {
    Transaction save(Transaction transaction);
    void delete(Transaction transaction);
    List<Transaction> findByUser(User user);
    Transaction findById(Long id);
}
