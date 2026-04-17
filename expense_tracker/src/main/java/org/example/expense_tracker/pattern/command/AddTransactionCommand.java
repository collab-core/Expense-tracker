package org.example.expense_tracker.pattern.command;

import org.example.expense_tracker.model.Transaction;
import org.example.expense_tracker.service.TransactionService;

public class AddTransactionCommand implements Command {

    private final TransactionService transactionService;
    private final Transaction transaction;

    public AddTransactionCommand(TransactionService transactionService, Transaction transaction) {
        this.transactionService = transactionService;
        this.transaction = transaction;
    }

    @Override
    public void execute() {
        transactionService.saveDirectTransaction(transaction);
    }

    @Override
    public void undo() {
        transactionService.deleteSpecificTransaction(transaction);
    }
}
