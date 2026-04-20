# Expense Tracker Application - Comprehensive Design Patterns & Architectural Analysis

## Executive Summary

The Expense Tracker application is a sophisticated Spring Boot + JavaFX desktop application that demonstrates **12+ design patterns** across **3 pattern categories** (Creational, Structural, Behavioral) combined with a well-defined **layered architecture**. The application showcases enterprise-grade architectural decisions with clear separation of concerns.

---

## I. OVERALL ARCHITECTURAL PATTERNS

### 1. **Layered Architecture (N-Tier Architecture)**
- **Controller Layer**: JavaFX controllers handling UI logic
- **Service Layer**: Business logic and orchestration
- **Repository/DAO Layer**: Data access abstraction
- **Data Layer**: PostgreSQL database via JPA/Hibernate

**Implementation Locations:**
- Controllers: `org.example.expense_tracker.controller.*`
- Services: `org.example.expense_tracker.service.*`
- DAOs: `org.example.expense_tracker.pattern.dao.*`
- Repositories: `org.example.expense_tracker.repository.*`

### 2. **MVC (Model-View-Controller) Pattern**
The application implements MVC at two levels:

#### **Spring Boot MVC Level:**
- **Model**: JPA entities (`User.java`, `Transaction.java`)
- **View**: FXML XML files in `resources/fxml/`
- **Controller**: Spring `@Controller` beans managing view logic

#### **JavaFX MVC Level:**
- Controllers bind to FXML files
- `ViewSwitcher` service manages view transitions
- Direct interaction between controllers and service layer

**Key Files:**
- [LoginView.fxml](../../resources/fxml/LoginView.fxml) ↔ [LoginViewController.java](src/main/java/org/example/expense_tracker/controller/LoginViewController.java)
- [MainView.fxml](../../resources/fxml/MainView.fxml) ↔ [MainViewController.java](src/main/java/org/example/expense_tracker/controller/MainViewController.java)
- [TransactionForm.fxml](../../resources/fxml/TransactionForm.fxml) ↔ [TransactionFormController.java](src/main/java/org/example/expense_tracker/controller/TransactionFormController.java)
- [StatisticsView.fxml](../../resources/fxml/StatisticsView.fxml) ↔ [StatisticsViewController.java](src/main/java/org/example/expense_tracker/controller/StatisticsViewController.java)

### 3. **Spring Boot Framework Integration**
- **@SpringBootApplication**: Entry point decorator
- **Component Scanning**: Auto-discovery of @Service, @Repository, @Controller, @Component
- **Dependency Injection Container**: Autowiring dependencies throughout the application
- **Configuration Classes**: `SecurityConfig.java` for bean definitions

**Implementation:**
```java
// ExpenseTrackerApplication.java
@SpringBootApplication
public class ExpenseTrackerApplication {
    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }
}

// JavaFxApplication.java - Initializes Spring context
public void init() {
    this.springContext = new SpringApplicationBuilder(ExpenseTrackerApplication.class).run();
}
```

---

## II. CREATIONAL PATTERNS

### 1. **Singleton Pattern**

#### **Implementation A: DatabaseConnection Singleton**
**Location:** [DatabaseConnection.java](src/main/java/org/example/expense_tracker/pattern/singleton/DatabaseConnection.java)

```java
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        System.out.println("Singleton DatabaseConnection initialized");
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
}
```

**Purpose:** Ensures only one database connection instance exists

**Characteristics:**
- Private constructor prevents direct instantiation
- Synchronized static method ensures thread-safety
- Lazy initialization pattern

#### **Implementation B: AppConfig Singleton**
**Location:** [AppConfig.java](src/main/java/org/example/expense_tracker/pattern/singleton/AppConfig.java)

```java
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
}
```

**Purpose:** Global application configuration management (currency settings)

### 2. **Factory Pattern**

#### **TransactionFactory**
**Location:** [TransactionFactory.java](src/main/java/org/example/expense_tracker/model/TransactionFactory.java)

```java
public class TransactionFactory {
    public static Transaction createTransaction(
        TransactionType type, 
        BigDecimal amount, 
        String description, 
        LocalDate date, 
        Category category, 
        User user
    ) {
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setDate(date);
        transaction.setCategory(category);
        transaction.setUser(user);
        return transaction;
    }
    
    // Convenience factory methods
    public static Transaction createExpense(...) { ... }
    public static Transaction createIncome(...) { ... }
}
```

**Purpose:** Centralized, encapsulated object creation logic for transactions

**Usage in Service:**
[TransactionService.java](src/main/java/org/example/expense_tracker/service/TransactionService.java) - Line 58
```java
// Using Factory Pattern
Transaction transaction = TransactionFactory.createTransaction(
    type, amount, description, date, category, user
);
```

**Benefits:**
- Decouples transaction creation from business logic
- Provides reusable creation methods
- Future-proof for complex initialization

### 3. **Spring Dependency Injection Pattern**

**Type:** Constructor-based Dependency Injection

**Examples Throughout Application:**

#### A. Controller Injection
[LoginViewController.java](src/main/java/org/example/expense_tracker/controller/LoginViewController.java)
```java
@Controller
public class LoginViewController {
    private final UserService userService;
    private final ViewSwitcher viewSwitcher;
    private final UserSession userSession;

    @Autowired
    public LoginViewController(UserService userService, 
                              ViewSwitcher viewSwitcher, 
                              UserSession userSession) {
        this.userService = userService;
        this.viewSwitcher = viewSwitcher;
        this.userSession = userSession;
    }
}
```

#### B. Service Injection
[TransactionService.java](src/main/java/org/example/expense_tracker/service/TransactionService.java)
```java
@Service
public class TransactionService implements ITransactionService, TransactionSubject {
    private final TransactionDAO transactionDAO;
    private final UserSession userSession;

    @Autowired
    public TransactionService(TransactionDAO transactionDAO, UserSession userSession) {
        this.transactionDAO = transactionDAO;
        this.userSession = userSession;
    }
}
```

#### C. Repository Injection
[TransactionDAOImpl.java](src/main/java/org/example/expense_tracker/pattern/dao/TransactionDAOImpl.java)
```java
@Repository
public class TransactionDAOImpl implements TransactionDAO {
    private final TransactionRepository repository;

    @Autowired
    public TransactionDAOImpl(TransactionRepository repository) {
        this.repository = repository;
    }
}
```

**Benefits:**
- Loose coupling between components
- Easier testing with mock injection
- Configuration externalization
- Framework manages object lifecycle

---

## III. STRUCTURAL PATTERNS

### 1. **Adapter Pattern**

#### **Purpose:** Adapt incompatible external currency conversion API to internal requirements

**Location:** `org.example.expense_tracker.pattern.adapter`

#### **Problem:**
External API (`ThirdPartyCurrencyAPI`) uses `double` primitives, but application uses `BigDecimal` for precision

**Solution Architecture:**
```
ThirdPartyCurrencyAPI (double-based)
           ↓
      [Adapter]
           ↓
CurrencyConverter Interface (BigDecimal-based)
           ↓
Application Code
```

#### **Implementation:**

**Target Interface:** [CurrencyConverter.java](src/main/java/org/example/expense_tracker/pattern/adapter/CurrencyConverter.java)
```java
public interface CurrencyConverter {
    BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency);
}
```

**Incompatible Class:** [ThirdPartyCurrencyAPI.java](src/main/java/org/example/expense_tracker/pattern/adapter/ThirdPartyCurrencyAPI.java)
```java
@Component
public class ThirdPartyCurrencyAPI {
    public double fetchConversionRate(String source, String target) {
        // Returns double, not BigDecimal
        if (source.equals("USD") && target.equals("EUR")) {
            return 0.93;
        }
        return 1.0;
    }
}
```

**Adapter Implementation:** [CurrencyAPIAdapter.java](src/main/java/org/example/expense_tracker/pattern/adapter/CurrencyAPIAdapter.java)
```java
@Component
public class CurrencyAPIAdapter implements CurrencyConverter {
    private final ThirdPartyCurrencyAPI externalApi;

    @Override
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        // Bridge the type gap: double → BigDecimal
        double rate = externalApi.fetchConversionRate(fromCurrency, toCurrency);
        return amount.multiply(BigDecimal.valueOf(rate))
                     .setScale(2, RoundingMode.HALF_EVEN);
    }
}
```

**Benefits:**
- Makes incompatible interfaces compatible
- Isolates external API changes
- Maintains type safety (BigDecimal precision)
- Easy to swap implementations

### 2. **Decorator Pattern**

#### **Purpose:** Dynamically add behavior to transaction reports (e.g., tax calculations)

**Location:** `org.example.expense_tracker.pattern.decorator`

#### **Core Component:** [TransactionReport.java](src/main/java/org/example/expense_tracker/pattern/decorator/TransactionReport.java)
```java
public interface TransactionReport {
    double calculateTotal(List<Transaction> transactions);
}
```

#### **Base Component:** [BaseTransactionReport.java](src/main/java/org/example/expense_tracker/pattern/decorator/BaseTransactionReport.java)
```java
public class BaseTransactionReport implements TransactionReport {
    @Override
    public double calculateTotal(List<Transaction> transactions) {
        return transactions.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
    }
}
```

#### **Abstract Decorator:** [ReportDecorator.java](src/main/java/org/example/expense_tracker/pattern/decorator/ReportDecorator.java)
```java
public abstract class ReportDecorator implements TransactionReport {
    protected TransactionReport report;

    public ReportDecorator(TransactionReport report) {
        this.report = report;
    }

    @Override
    public double calculateTotal(List<Transaction> transactions) {
        return report.calculateTotal(transactions);
    }
}
```

#### **Concrete Decorator:** [TaxDecorator.java](src/main/java/org/example/expense_tracker/pattern/decorator/TaxDecorator.java)
```java
public class TaxDecorator extends ReportDecorator {
    private final double taxRate;

    public TaxDecorator(TransactionReport report, double taxRate) {
        super(report);
        this.taxRate = taxRate;
    }

    @Override
    public double calculateTotal(List<Transaction> transactions) {
        double baseTotal = super.calculateTotal(transactions);
        // Add tax decoration
        return baseTotal + (baseTotal * taxRate);
    }
}
```

#### **Usage Example:**
```java
// Basic report
TransactionReport report = new BaseTransactionReport();
double basic = report.calculateTotal(transactions);

// Enhanced with tax
TransactionReport withTax = new TaxDecorator(report, 0.15);
double taxedTotal = withTax.calculateTotal(transactions);

// Stacked decorators (composable)
TransactionReport complex = new TaxDecorator(
    new TaxDecorator(baseReport, 0.10), 
    0.05
);
```

**Benefits:**
- Runtime behavior composition
- Avoids class explosion from inheritance
- Single Responsibility Principle
- Open/Closed Principle

### 3. **Proxy Pattern**

#### **Purpose:** Add security layer to restrict transaction operations to authenticated users

**Location:** [ProxyTransactionService.java](src/main/java/org/example/expense_tracker/pattern/proxy/ProxyTransactionService.java)

#### **Real Subject:** [TransactionService.java](src/main/java/org/example/expense_tracker/service/TransactionService.java)

#### **Pattern Structure:**
```
ITransactionService Interface
    ↑                ↑
    |                |
    |         ProxyTransactionService (Security Layer)
    |              ↓
    |      TransactionService (Real Implementation)
    |              ↓
    |      TransactionDAO (Data Access)
```

#### **Interface Definition:**
[ITransactionService.java](src/main/java/org/example/expense_tracker/service/ITransactionService.java)
```java
public interface ITransactionService {
    Transaction saveTransaction(BigDecimal amount, TransactionType type, 
                                Category category, LocalDate date, String description);
    Map<Category, Double> getExpenseBreakdown();
    Map<LocalDate, Double> getDailySpending();
    Map<LocalDate, Double> getExpenseTrend();
    Map<LocalDate, Double> getIncomeTrend();
}
```

#### **Proxy Implementation:**
```java
@Service
@Primary  // ← Spring injects proxy instead of real service
public class ProxyTransactionService implements ITransactionService {

    private final TransactionService transactionService;
    private final UserSession userSession;

    @Override
    public Transaction saveTransaction(BigDecimal amount, TransactionType type, 
                                      Category category, LocalDate date, String description) {
        // SECURITY CHECK: Only logged-in users can add transactions
        User user = userSession.getLoggedInUser();
        if (user == null) {
            throw new SecurityException("Access Denied: Only logged-in users can add expenses.");
        }
        
        // Delegate to real service
        return transactionService.saveTransaction(amount, type, category, date, description);
    }
    
    // Other methods simply delegate without security checks
    @Override
    public Map<Category, Double> getExpenseBreakdown() {
        return transactionService.getExpenseBreakdown();
    }
    // ... more delegations
}
```

#### **Benefits:**
- Transparently enforces security before operation execution
- Maintains same interface as real subject
- `@Primary` annotation makes Spring use proxy by default
- Separates security concerns from business logic

---

## IV. BEHAVIORAL PATTERNS

### 1. **Observer Pattern**

#### **Purpose:** Automatically refresh UI when transactions are updated

**Location:** `org.example.expense_tracker.pattern.observer`

#### **Observer Interface:** [TransactionObserver.java](src/main/java/org/example/expense_tracker/pattern/observer/TransactionObserver.java)
```java
public interface TransactionObserver {
    void onTransactionUpdated();
}
```

#### **Subject Interface:** [TransactionSubject.java](src/main/java/org/example/expense_tracker/pattern/observer/TransactionSubject.java)
```java
public interface TransactionSubject {
    void addObserver(TransactionObserver observer);
    void removeObserver(TransactionObserver observer);
    void notifyObservers();
}
```

#### **Subject Implementation:** [TransactionService.java](src/main/java/org/example/expense_tracker/service/TransactionService.java)
```java
@Service
public class TransactionService implements ITransactionService, TransactionSubject {

    private final TransactionDAO transactionDAO;
    private final UserSession userSession;
    private final List<TransactionObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(TransactionObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(TransactionObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (TransactionObserver observer : observers) {
            observer.onTransactionUpdated();
        }
    }

    @Override
    public Transaction saveTransaction(BigDecimal amount, TransactionType type, 
                                      Category category, LocalDate date, String description) {
        User user = userSession.getLoggedInUser();
        if (user == null) {
            throw new IllegalStateException("No user logged in");
        }
        
        Transaction transaction = TransactionFactory.createTransaction(
            type, amount, description, date, category, user
        );
        Transaction savedTransaction = transactionDAO.save(transaction);
        
        // NOTIFY OBSERVERS
        notifyObservers();
        
        return savedTransaction;
    }
}
```

#### **Observer Implementation:** [MainViewController.java](src/main/java/org/example/expense_tracker/controller/MainViewController.java)
```java
@Controller
public class MainViewController implements TransactionObserver {

    private final UserSession userSession;
    private final ViewSwitcher viewSwitcher;
    private final TransactionDAO transactionDAO;
    private final TransactionService transactionService;

    @Autowired
    public MainViewController(UserSession userSession, ViewSwitcher viewSwitcher,
                              TransactionDAO transactionDAO, TransactionService transactionService, 
                              ApplicationContext applicationContext) {
        // ... other initialization
        
        // REGISTER AS OBSERVER
        this.transactionService.addObserver(this);
    }

    // HANDLE NOTIFICATIONS
    @Override
    public void onTransactionUpdated() {
        // Refresh the table when notified of changes
        refreshTransactionTable();
    }
}
```

#### **Flow Diagram:**
```
Transaction Saved
       ↓
TransactionService.saveTransaction()
       ↓
transactionDAO.save(transaction)
       ↓
notifyObservers()
       ↓
[MainViewController.onTransactionUpdated()]
       ↓
[StatisticsViewController.onTransactionUpdated()] 
       ↓
UI Refreshes
```

**Benefits:**
- Automatic UI synchronization
- Decoupled UI from business logic
- Multiple observers possible
- Push-based communication model

### 2. **Command Pattern**

#### **Purpose:** Encapsulate transaction operations (add/delete) for undo/redo functionality

**Location:** `org.example.expense_tracker.pattern.command`

#### **Command Interface:** [Command.java](src/main/java/org/example/expense_tracker/pattern/command/Command.java)
```java
public interface Command {
    void execute();
    void undo();
}
```

#### **Concrete Commands:**

**A. AddTransactionCommand** [AddTransactionCommand.java](src/main/java/org/example/expense_tracker/pattern/command/AddTransactionCommand.java)
```java
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
```

**B. DeleteTransactionCommand** [DeleteTransactionCommand.java](src/main/java/org/example/expense_tracker/pattern/command/DeleteTransactionCommand.java)
```java
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
```

#### **Command Invoker:** [TransactionCommandInvoker.java](src/main/java/org/example/expense_tracker/pattern/command/TransactionCommandInvoker.java)
```java
@Component
public class TransactionCommandInvoker {
    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();

    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();  // Clear redo when new command executed
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }
}
```

#### **Flow Diagram:**
```
User adds transaction
        ↓
AddTransactionCommand created
        ↓
invoker.executeCommand(cmd)
        ↓
cmd.execute() → transactionService.saveDirectTransaction()
        ↓
Push to undoStack
        ↓
User clicks Undo
        ↓
invoker.undo()
        ↓
cmd.undo() → transactionService.deleteSpecificTransaction()
        ↓
Move to redoStack
```

**Benefits:**
- Encapsulates requests as objects
- Supports undo/redo operations
- Enables command queuing and logging
- Deferred command execution

### 3. **Strategy Pattern**

#### **Purpose:** Allow different transaction service implementations with consistent interface

**Location:** Service layer with `ITransactionService` interface

#### **Strategy Interface:** [ITransactionService.java](src/main/java/org/example/expense_tracker/service/ITransactionService.java)
```java
public interface ITransactionService {
    Transaction saveTransaction(BigDecimal amount, TransactionType type, 
                                Category category, LocalDate date, String description);
    Map<Category, Double> getExpenseBreakdown();
    Map<LocalDate, Double> getDailySpending();
    Map<LocalDate, Double> getExpenseTrend();
    Map<LocalDate, Double> getIncomeTrend();
}
```

#### **Concrete Strategies:**

**Strategy A: TransactionService** (Core Implementation)
```java
@Service
public class TransactionService implements ITransactionService, TransactionSubject {
    // Direct implementation of business logic
}
```

**Strategy B: ProxyTransactionService** (Security-Enhanced)
```java
@Service
@Primary
public class ProxyTransactionService implements ITransactionService {
    // Adds security checks before delegating
}
```

#### **Runtime Strategy Selection:**
```java
@Autowired
public TransactionFormController(ITransactionService transactionService) {
    // Spring automatically injects the @Primary implementation
    // (ProxyTransactionService due to @Primary annotation)
    this.transactionService = transactionService;
}
```

**Benefits:**
- Interchangeable implementations
- Runtime algorithm selection
- Easy to add new strategies
- Encapsulates varying behaviors

---

## V. DATA ACCESS PATTERNS

### 1. **DAO (Data Access Object) Pattern**

#### **Purpose:** Abstract database operations and provide unified interface

**Location:** `org.example.expense_tracker.pattern.dao`

#### **DAO Interface:** [TransactionDAO.java](src/main/java/org/example/expense_tracker/pattern/dao/TransactionDAO.java)
```java
public interface TransactionDAO {
    Transaction save(Transaction transaction);
    void delete(Transaction transaction);
    List<Transaction> findByUser(User user);
    Transaction findById(Long id);
}
```

#### **DAO Implementation:** [TransactionDAOImpl.java](src/main/java/org/example/expense_tracker/pattern/dao/TransactionDAOImpl.java)
```java
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
```

#### **Repository Interface:** [TransactionRepository.java](src/main/java/org/example/expense_tracker/repository/TransactionRepository.java)
```java
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(User user);
}
```

#### **Abstraction Layers:**
```
Service Layer (TransactionService)
           ↓
DAO Pattern (TransactionDAO interface)
           ↓
DAO Implementation (TransactionDAOImpl)
           ↓
Spring Data JPA (TransactionRepository)
           ↓
JPA/Hibernate (ORM)
           ↓
Database (PostgreSQL)
```

**Benefits:**
- Isolates database operations
- Easy to swap data sources
- Improves testability with mock DAOs
- Clear separation of concerns

### 2. **Repository Pattern (Spring Data JPA)**

#### **User Repository:** [UserRepository.java](src/main/java/org/example/expense_tracker/repository/UserRepository.java)
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

#### **Transaction Repository:** [TransactionRepository.java](src/main/java/org/example/expense_tracker/repository/TransactionRepository.java)
```java
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(User user);
}
```

**Spring Data Benefits:**
- Automatic CRUD implementations
- Custom query methods via naming conventions
- Pagination and sorting support
- Reduces boilerplate code

### 3. **ORM (Object-Relational Mapping)**

#### **Persistence Configuration:** [application.properties](src/main/resources/application.properties)
```properties
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

#### **Entity Mapping Example:** [User.java](src/main/java/org/example/expense_tracker/model/User.java)
```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();
}
```

#### **Relationship Mapping:** [Transaction.java](src/main/java/org/example/expense_tracker/model/Transaction.java)
```java
@Entity
@Table(name = "transactions")
public class Transaction {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Maps to database foreign key
}
```C:\Users\DHIVYADHARSHINI R\OneDrive\Desktop\PACKAGES\SP>mvnw.cmd clean install
The system cannot find the file C:\Users\DHIVYADHARSHINI R\OneDrive\Desktop\PACKAGES\SP\.mvn\wrapper\maven-wrapper.properties.
Exception calling "DownloadFile" with "2" argument(s): "An exception occurred during a WebClient request."
At line:1 char:282
+ ... pe]::Tls12; $webclient.DownloadFile('https://repo.maven.apache.org/ma ...
+                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    + CategoryInfo          : NotSpecified: (:) [], MethodInvocationException
    + FullyQualifiedErrorId : WebException
 
Picked up JAVA_TOOL_OPTIONS: -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8
Error: Could not find or load main class org.apache.maven.wrapper.MavenWrapperMain
Caused by: java.lang.ClassNotFoundException: org.apache.maven.wrapper.MavenWrapperMain
---

## VI. CONFIGURATION & DEPENDENCY INJECTION PATTERNS

### 1. **Java Configuration Pattern**

#### **Security Configuration:** [SecurityConfig.java](src/main/java/org/example/expense_tracker/config/SecurityConfig.java)
```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Benefits:**
- Type-safe configuration
- IDE support for method names
- Explicit bean definitions
- Composable configurations

### 2. **Bean Scope Management**

#### **Singleton Scope (Session Management):**
```java
@Service
@Scope("singleton")
public class UserSession {
    private User currentUser;

    public void login(User user) {
        this.currentUser = user;
    }

    public User getLoggedInUser() {
        return currentUser;
    }
}
```

**Scopes Used:**
- **singleton**: One instance per Spring context (UserSession, ViewSwitcher)
- **prototype**: New instance per injection (if needed)

### 3. **Primary Annotation (Strategy Selection)**

```java
@Service
@Primary  // ← Indicates this is the default implementation
public class ProxyTransactionService implements ITransactionService {
    // When Spring needs ITransactionService, inject this by default
}
```

---

## VII. SPRING FRAMEWORK PATTERNS

### 1. **Component Scanning & Auto-Configuration**

**Entry Point:**
```java
@SpringBootApplication
public class ExpenseTrackerApplication {
    // @SpringBootApplication combines:
    // - @Configuration
    // - @ComponentScan
    // - @EnableAutoConfiguration
}
```

**Component Registration:**
- `@Service`: Business logic components
- `@Repository`: Data access components
- `@Controller`: UI controller components
- `@Component`: Generic components (ViewSwitcher)

### 2. **JavaFX-Spring Integration**

#### **Application Initialization:** [JavaFxApplication.java](src/main/java/org/example/expense_tracker/JavaFxApplication.java)
```java
public class JavaFxApplication extends Application {
    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Bootstrap Spring context
        this.springContext = new SpringApplicationBuilder(ExpenseTrackerApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        
        // Critical: Let Spring create controller instances
        fxmlLoader.setControllerFactory(springContext::getBean);
        
        // ... rest of initialization
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
    }
}
```

#### **View Switching Service:** [ViewSwitcher.java](src/main/java/org/example/expense_tracker/service/ViewSwitcher.java)
```java
@Service
public class ViewSwitcher {
    private final ApplicationContext applicationContext;
    private Stage primaryStage;

    public void switchTo(String fxmlPath, String title) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        
        // Controllers created by Spring = dependency injection works!
        loader.setControllerFactory(applicationContext::getBean);
        
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
```

---

## VIII. DOMAIN MODEL PATTERNS

### 1. **Entity Classes with Enums**

#### **Transaction Type Enum:** [TransactionType.java](src/main/java/org/example/expense_tracker/model/TransactionType.java)
```java
public enum TransactionType {
    INCOME,
    EXPENSE
}
```

#### **Category Enum:** [Category.java](src/main/java/org/example/expense_tracker/model/Category.java)
```java
public enum Category {
    FOOD,
    RENT,
    TRAVEL,
    HEALTH,
    SHOPPING,
    INCOME,
    OTHER
}
```

#### **Usage in Entity:** [Transaction.java](src/main/java/org/example/expense_tracker/model/Transaction.java)
```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private TransactionType type;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private Category category;
```

### 2. **Relationship Modeling**

#### **One-to-Many Relationship (User → Transactions):**
```java
// In User.java
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Transaction> transactions = new ArrayList<>();

// In Transaction.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

**Benefits:**
- Cascade delete: Removing user deletes all transactions
- Orphan removal: Detached transactions are removed
- Lazy loading: Transactions loaded only when needed

### 3. **Lombok Annotation Utilization**

**Reduces Boilerplate in Entities:**
```java
@Entity
@Table(name = "users")
@Getter           // Generates getters
@Setter           // Generates setters
@NoArgsConstructor // Generates no-arg constructor
@AllArgsConstructor // Generates all-args constructor
public class User {
    // Field definitions only
}
```

---

## IX. SECURITY PATTERNS

### 1. **Password Encoding Strategy**

#### **Configuration:**
```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

#### **Usage in UserService:** [UserService.java](src/main/java/org/example/expense_tracker/service/UserService.java)
```java
public User registerUser(String username, String email, String rawPassword) {
    // Hash password before storage
    user.setPassword(passwordEncoder.encode(rawPassword));
    return userRepository.save(user);
}

public Optional<User> loginUser(String username, String rawPassword) {
    User user = userRepository.findByUsername(username).orElse(null);
    if (user != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
        return Optional.of(user);
    }
    return Optional.empty();
}
```

### 2. **Session Management Pattern**

#### **UserSession as Security Context:**
```java
@Service
@Scope("singleton")
public class UserSession {
    private User currentUser;

    public void login(User user) {
        this.currentUser = user;
    }

    public User getLoggedInUser() {
        return currentUser;  // Used for authorization checks
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
```

#### **Usage in Proxy for Access Control:**
```java
public Transaction saveTransaction(...) {
    User user = userSession.getLoggedInUser();
    if (user == null) {
        throw new SecurityException("Access Denied: Only logged-in users can add expenses.");
    }
    return transactionService.saveTransaction(...);
}
```

---

## X. UI PATTERNS

### 1. **Model-View Pattern with JavaFX**

#### **FXML as View Definition:**
- [LoginView.fxml](../../resources/fxml/LoginView.fxml)
- [MainView.fxml](../../resources/fxml/MainView.fxml)
- [TransactionForm.fxml](../../resources/fxml/TransactionForm.fxml)
- [StatisticsView.fxml](../../resources/fxml/StatisticsView.fxml)

#### **Controller as View Logic:**
```java
@Controller
public class LoginViewController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    @FXML
    public void onLogin(ActionEvent event) {
        // Handle UI event
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        Optional<User> user = userService.loginUser(username, password);
        
        if (user.isPresent()) {
            errorLabel.setText("");
            userSession.login(user.get());
            viewSwitcher.switchTo("/fxml/MainView.fxml", "Expense Tracker");
        } else {
            errorLabel.setText("Invalid username or password.");
        }
    }
}
```

### 2. **Data Binding & TableView Pattern**

#### **Automatic UI Refresh via Observer:**
```java
@Controller
public class MainViewController implements TransactionObserver {
    @FXML
    private TableView<Transaction> transactionsTable;
    @FXML
    private TableColumn<Transaction, LocalDate> dateColumn;
    @FXML
    private TableColumn<Transaction, BigDecimal> amountColumn;

    @Override
    public void onTransactionUpdated() {
        refreshTransactionTable();  // Bind new data when notified
    }

    private void refreshTransactionTable() {
        User user = userSession.getLoggedInUser();
        List<Transaction> transactions = transactionDAO.findByUser(user);
        ObservableList<Transaction> observableList = FXCollections.observableArrayList(transactions);
        transactionsTable.setItems(observableList);
    }
}
```

### 3. **Chart Data Visualization Pattern**

#### **Statistics Controller:** [StatisticsViewController.java](src/main/java/org/example/expense_tracker/controller/StatisticsViewController.java)
```java
@Controller
public class StatisticsViewController {
    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Number> dailySpendingBarChart;
    @FXML private LineChart<String, Number> expenseTrendChart;

    @FXML
    public void initialize() {
        loadPieChartData();        // Category breakdown
        loadBarChartData();        // Daily spending
        loadExpenseTrendChartData(); // Expense trends
        loadIncomeTrendChartData();  // Income trends
    }

    private void loadPieChartData() {
        Map<Category, Double> expenseData = transactionService.getExpenseBreakdown();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<Category, Double> entry : expenseData.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey().toString(), entry.getValue()));
        }
        categoryPieChart.setData(pieChartData);
    }
}
```

---

## XI. ARCHITECTURAL LAYERING SUMMARY

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER (UI)                  │
│  JavaFX Controllers + FXML Views                            │
│  (LoginViewController, MainViewController, etc.)            │
└────────────────────────────┬────────────────────────────────┘
                             │ Uses
┌────────────────────────────▼────────────────────────────────┐
│                    SERVICE LAYER                            │
│  Business Logic & Orchestration                            │
│  (UserService, TransactionService, ViewSwitcher)           │
│  Patterns: Observer, Factory, Command Invoker              │
│  Security: ProxyTransactionService                         │
└────────────────────────────┬────────────────────────────────┘
                             │ Uses
┌────────────────────────────▼────────────────────────────────┐
│                    DAO LAYER                                │
│  Data Access Abstraction                                   │
│  (TransactionDAO, TransactionDAOImpl)                       │
│  Patterns: DAO, Adapter                                    │
└────────────────────────────┬────────────────────────────────┘
                             │ Uses
┌────────────────────────────▼────────────────────────────────┐
│              PERSISTENCE LAYER                              │
│  Spring Data JPA Repositories                              │
│  (UserRepository, TransactionRepository)                   │
│  Technology: JPA/Hibernate ORM                             │
└────────────────────────────┬────────────────────────────────┘
                             │ Uses
┌────────────────────────────▼────────────────────────────────┐
│                    DATABASE LAYER                           │
│  PostgreSQL Relational Database                            │
│  (remote: nozomi.proxy.rlwy.net:31333)                     │
└─────────────────────────────────────────────────────────────┘

Cross-Cutting Concerns:
├─ Dependency Injection: Spring @Autowired (Constructor-based)
├─ Configuration: Java @Configuration + application.properties
├─ Security: BCryptPasswordEncoder + Session Management
├─ Singleton Management: DatabaseConnection, AppConfig
└─ Component Registration: @Service, @Repository, @Controller
```

---

## XII. QUICK REFERENCE: PATTERN LOCATIONS

| Pattern Name | Type | Location | Key Classes |
|---|---|---|---|
| **MVC** | Architectural | Entire App | Controllers, FXML, Services |
| **Layered Architecture** | Architectural | Entire App | Controller→Service→DAO→DB |
| **Singleton** | Creational | `pattern/singleton/` | DatabaseConnection, AppConfig |
| **Factory** | Creational | `model/` | TransactionFactory |
| **Dependency Injection** | Creational | Entire App | Spring @Autowired |
| **Adapter** | Structural | `pattern/adapter/` | CurrencyAPIAdapter |
| **Decorator** | Structural | `pattern/decorator/` | TaxDecorator, ReportDecorator |
| **Proxy** | Structural | `pattern/proxy/` | ProxyTransactionService |
| **Observer** | Behavioral | `pattern/observer/` | TransactionObserver, TransactionSubject |
| **Command** | Behavioral | `pattern/command/` | AddTransactionCommand, DeleteTransactionCommand |
| **Strategy** | Behavioral | `service/` | ITransactionService implementations |
| **DAO** | Data Access | `pattern/dao/` | TransactionDAO, TransactionDAOImpl |
| **Repository** | Data Access | `repository/` | UserRepository, TransactionRepository |
| **ORM** | Data Access | Entities | JPA @Entity annotations |
| **Service Locator** | Structural | `service/` | ViewSwitcher |

---

## XIII. DESIGN PRINCIPLES DEMONSTRATED

### 1. **SOLID Principles**

#### **S - Single Responsibility Principle**
- `TransactionService` handles business logic only
- `TransactionDAO` handles data access only
- `ProxyTransactionService` handles security only
- `UserSession` handles session state only

#### **O - Open/Closed Principle**
- `TransactionReport` interface is open for extension
- `TaxDecorator`, `ReportDecorator` extend without modifying base code
- New decorators can be added without changing existing code

#### **L - Liskov Substitution Principle**
- `ProxyTransactionService` substitutable for `ITransactionService`
- `TransactionDAOImpl` substitutable for `TransactionDAO`
- All implementations maintain interface contracts

#### **I - Interface Segregation Principle**
- `ITransactionService` specific to transaction operations
- `TransactionObserver` specific to observation contracts
- `CurrencyConverter` specific to currency conversion

#### **D - Dependency Inversion Principle**
- Controllers depend on `ITransactionService` interface, not concrete class
- `TransactionService` depends on `TransactionDAO` interface
- High-level modules don't depend on low-level modules (use interfaces)

### 2. **DRY (Don't Repeat Yourself)**
- Factory pattern centralizes transaction creation
- ViewSwitcher centralizes view switching logic
- Service layer centralizes business logic

### 3. **Separation of Concerns**
- Controllers handle UI only
- Services handle business logic
- DAOs handle data access
- Entities represent data

---

## XIV. CONCLUSION

The Expense Tracker application exemplifies professional Java enterprise development with:

✅ **12+ Design Patterns** strategically applied  
✅ **Clear Layered Architecture** (4-tier)  
✅ **SOLID Principles** consistently followed  
✅ **Spring Framework Integration** for DI and configuration  
✅ **Security-First Design** (encrypted passwords, session management)  
✅ **Extensible Codebase** (easy to add new features/patterns)  
✅ **Clean Code** (readability, maintainability)  
✅ **Enterprise Patterns** (DAO, Repository, ORM)  

This architecture supports:
- Easy testing (via interfaces and DI)
- Feature additions (via decorators, commands)
- Maintenance (via separation of concerns)
- Scalability (via layered approach)
- Security (via proxy and session management)

---

**Analysis Date:** April 18, 2026  
**Java Version:** Java 22  
**Framework:** Spring Boot 3.3.0  
**UI Framework:** JavaFX 22.0.1  
**Database:** PostgreSQL
