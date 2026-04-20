package org.example.expense_tracker.pattern.dao;

import org.example.expense_tracker.model.Transaction;
import org.example.expense_tracker.model.User;
import org.example.expense_tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TransactionDAOImpl implements TransactionDAO {

    private final TransactionRepository repository;

    @Autowired
    public TransactionDAOImpl(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return repository.save(transaction);
    }

    @Override
    public void delete(Transaction transaction) {
        repository.delete(transaction);
    }

    @Override
    public List<Transaction> findByUser(User user) {
        return repository.findByUser(user);
    }

    @Override
    public Transaction findById(Long id) {
        return repository.findById(id).orElse(null);
    }
}
