package org.example.expense_tracker.pattern.singleton;

import org.springframework.stereotype.Component;

/**
 * Application configuration component managed by Spring as a singleton.
 * Spring automatically manages the singleton lifecycle, so manual synchronization is not needed.
 */
@Component
public class AppConfig {
    private String currencyCode = "USD";
    
    /**
     * Default constructor for Spring component initialization.
     * AppConfig is managed by Spring as a singleton component.
     */
    public AppConfig() {
        // Empty constructor required by Spring framework for component instantiation
        // AppConfig uses Spring's default singleton scope management
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
    
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
