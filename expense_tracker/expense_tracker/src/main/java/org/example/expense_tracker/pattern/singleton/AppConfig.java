package org.example.expense_tracker.pattern.singleton;

public class AppConfig {
    private static AppConfig instance;
    private String currencyCode = "USD";
    
    private AppConfig() {}

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }
    
    public String getCurrencyCode() {
        return currencyCode;
    }
    
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
