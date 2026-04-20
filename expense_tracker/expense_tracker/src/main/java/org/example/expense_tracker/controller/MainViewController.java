package org.example.expense_tracker.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.example.expense_tracker.model.Transaction;
import org.example.expense_tracker.model.TransactionType;
import org.example.expense_tracker.model.User;
import org.example.expense_tracker.pattern.dao.TransactionDAO; // <-- Make sure this is imported
import org.example.expense_tracker.pattern.observer.TransactionObserver;
import org.example.expense_tracker.service.TransactionService;
import org.example.expense_tracker.service.UserSession; // <-- NEW IMPORT
import org.example.expense_tracker.service.ViewSwitcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // <-- NEW IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

@Controller
public class MainViewController implements TransactionObserver {
    private static final Logger logger = LoggerFactory.getLogger(MainViewController.class);

    @FXML
    private BorderPane mainBorderPane;
    private Node transactionHistoryView;
    private final ApplicationContext applicationContext;

    @FXML
    private Label welcomeLabel;

    @FXML
    private MenuItem logoutMenuItem;

    @FXML
    private TableView<Transaction> transactionsTable;

    @FXML
    private TableColumn<Transaction, LocalDate> dateColumn;

    @FXML
    private TableColumn<Transaction, String> descriptionColumn;

    @FXML
    private TableColumn<Transaction, BigDecimal> amountColumn;

    @FXML
    private TableColumn<Transaction, String> categoryColumn;

    private final UserSession userSession;
    private final ViewSwitcher viewSwitcher;
    private final TransactionDAO transactionDAO;
    private final TransactionService transactionService;

    @Autowired
    public MainViewController(UserSession userSession, ViewSwitcher viewSwitcher,
                              TransactionDAO transactionDAO, TransactionService transactionService, ApplicationContext applicationContext) {
        this.userSession = userSession;
        this.viewSwitcher = viewSwitcher;
        this.transactionDAO = transactionDAO;
        this.transactionService = transactionService;
        this.applicationContext = applicationContext;
        
        // Register as an observer
        this.transactionService.addObserver(this);
    }

    @Override
    public void onTransactionUpdated() {
        // Refresh the table when notified
        refreshTransactionTable();
    }

    @FXML
    public void initialize() {
        User user = userSession.getLoggedInUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        }

        // Setup row styling
        transactionsTable.setRowFactory(tv -> new TableRow<Transaction>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(getTransactionRowStyle(item, empty));
            }
        });

        // Setup table columns
        dateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDate()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDescription() != null ? cellData.getValue().getDescription() : ""));
        amountColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAmount()));
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCategory() != null ? cellData.getValue().getCategory().toString() : ""));

        // Store the transaction history view
        transactionHistoryView = mainBorderPane.getCenter();

        // Load the Calendar as the default home screen
        handleViewCalendar(null);

        // Load table data
        refreshTransactionTable();
    }

    public void refreshTransactionTable() {
        User user = userSession.getLoggedInUser();
        if (user != null) {
            ObservableList<Transaction> transactions = FXCollections.observableArrayList(
                    transactionDAO.findByUser(user));
            transactionsTable.setItems(transactions);
        }
    }

    private String getTransactionRowStyle(Transaction item, boolean empty) {
        if (item == null || empty) {
            return "";
        }
        if (item.getDate().equals(LocalDate.now())) {
            return "-fx-background-color: #fff9c4;"; // Pale Yellow
        }
        if (item.getType() == TransactionType.INCOME) {
            return "-fx-background-color: #c8e6c9;"; // Light Green
        }
        if (item.getType() == TransactionType.EXPENSE) {
            return "-fx-background-color: #ffcdd2;"; // Light Red
        }
        return "";
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent view = fxmlLoader.load();
            mainBorderPane.setCenter(view);
        } catch (java.io.IOException e) {
            logger.error("Failed to load view: {}", fxmlFile, e);
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        userSession.logout();
        try {
            viewSwitcher.switchTo("/fxml/LoginView.fxml", "Login");
        } catch (RuntimeException e) {
            logger.error("Failed to switch to login view during logout", e);
            throw new RuntimeException("Logout failed: Unable to navigate to login view", e);
        }
    }

    @FXML
    private void handleViewCalendar(ActionEvent event) {
        logger.info("View Calendar menu clicked!");
        loadView("/fxml/CalendarView.fxml");
    }

    @FXML
    private void handleViewStatistics(ActionEvent event) {
        logger.info("View Statistics menu clicked!");
        loadView("/fxml/StatisticsView.fxml");
    }

    @FXML
    private void handleShowAbout(ActionEvent event) {
        logger.info("Show About menu clicked!");
        loadView("/fxml/AboutView.fxml");
    }

    // --- THIS METHOD IS NOW PUBLIC ---
    @FXML
    public void handleViewTransactionHistory(ActionEvent event) {
        logger.info("View Transaction History clicked!");
        mainBorderPane.setCenter(transactionHistoryView);
        refreshTransactionTable();
    }
}