package org.example.expense_tracker.pattern.observer;

public interface TransactionSubject {
    void addObserver(TransactionObserver observer);
    void removeObserver(TransactionObserver observer);
    void notifyObservers();
}
