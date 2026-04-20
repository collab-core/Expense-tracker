package org.example.expense_tracker.pattern.command;

import org.example.expense_tracker.model.Transaction;
import org.example.expense_tracker.service.TransactionService;

public class DeleteTransactionCommand implements Command {

    private final TransactionService transactionService;
    private final Transaction transaction;

    public DeleteTransactionCommand(TransactionService transactionService, Transaction transaction) {
        this.transactionService = transactionService;
        this.transaction = transaction;
    }

    @Override
    public void execute() {
        transactionService.deleteSpecificTransaction(transaction);
    }

    @Override
    public void undo() {
        transactionService.saveDirectTransaction(transaction);
    }
}
