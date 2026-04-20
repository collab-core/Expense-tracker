package org.example.expense_tracker.pattern.command;

public interface Command {
    void execute();
    void undo();
}
