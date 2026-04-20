package org.example.expense_tracker.pattern.singleton;

import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Database connection component managed by Spring as a singleton.
 * Spring automatically manages the singleton lifecycle, ensuring a single instance
 * of this component is created and shared across the application.
 */
@Component
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private Connection connection;

    public DatabaseConnection() {
        // Empty constructor required by Spring framework for component instantiation
        // DatabaseConnection uses Spring's default singleton scope management
        logger.info("DatabaseConnection bean initialized by Spring");
    }

    public Connection getConnection() {
        return connection;
    }
}
