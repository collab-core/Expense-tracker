package org.example.expense_tracker;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class ExpenseTrackerApplication {
    public static void main(String[] args) {
        // Fix for "Asia/Calcutta" PostgreSQL connection issue
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        Application.launch(JavaFxApplication.class, args);
    }
}
