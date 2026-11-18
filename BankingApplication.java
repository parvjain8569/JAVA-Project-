import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// =========================================================================
// 1. CUSTOM EXCEPTIONS (Unit 1: Exception Handling)
// =========================================================================

class BankingException extends Exception {
    public BankingException(String message) {
        super(message);
    }
}

class InsufficientBalanceException extends BankingException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}

class InvalidAmountException extends BankingException {
    public InvalidAmountException(String message) {
        super(message);
    }
}

class AccountNotFoundException extends BankingException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}

// =========================================================================
// 2. ENUMS & TRANSACTION MODEL (Unit 1: Classes & Encapsulation)
// =========================================================================

enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_IN,
    TRANSFER_OUT
}

class Transaction {
    private final String transactionId;
    private final String accountNumber;
    private final TransactionType type;
    private final double amount;
    private final double balanceAfter;
    private final String targetAccountNumber;
    private final String description;
    private final LocalDateTime timestamp;

    public Transaction(String transactionId, String accountNumber, TransactionType type, 
                       double amount, double balanceAfter, String targetAccountNumber, String description) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.targetAccountNumber = targetAccountNumber;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public String getTransactionId() { return transactionId; }
    public String getAccountNumber() { return accountNumber; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getTargetAccountNumber() { return targetAccountNumber; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return String.format("[%s] ID: %s | Type: %-12s | Amount: Rs. %-9.2f | Bal: Rs. %-9.2f | %s",
                timestamp.format(formatter), transactionId, type, amount, balanceAfter, description);
    }
}

// =========================================================================
// 3. OOP ACCOUNT HIERARCHY (Unit 1: Abstraction, Inheritance & Polymorphism)
// =========================================================================

abstract class Account {
    private final String accountNumber;
    private final String customerName;
    private final String email;
    protected double balance;
    private final LocalDateTime createdAt;

    public Account(String accountNumber, String customerName, String email, double initialBalance) 
            throws InvalidAmountException {
        if (initialBalance < 0) {
            throw new InvalidAmountException("Initial deposit cannot be negative.");
        }
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.email = email;
        this.balance = initialBalance;
        this.createdAt = LocalDateTime.now();
    }

    public String getAccountNumber() { return accountNumber; }
    public String getCustomerName() { return customerName; }
    public String getEmail() { return email; }
    public double getBalance() { return balance; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero.");
        }
        this.balance += amount;
    }

    // Abstract methods overridden by child classes (Polymorphism)
    public abstract void withdraw(double amount) throws InsufficientBalanceException, InvalidAmountException;
    public abstract String getAccountType();
    public abstract String getAccountSummary();

    @Override
    public String toString() {
        return String.format("[%s] Acc No: %s | Holder: %s | Balance: Rs. %.2f",
                getAccountType(), accountNumber, customerName, balance);
    }
}

class SavingsAccount extends Account {
    public static final double MINIMUM_BALANCE = 1000.00;
    private final double interestRate = 4.00; // 4.0% p.a.

    public SavingsAccount(String accountNumber, String customerName, String email, double initialBalance) 
            throws InvalidAmountException, InsufficientBalanceException {
        super(accountNumber, customerName, email, initialBalance);
        if (initialBalance < MINIMUM_BALANCE) {
            throw new InsufficientBalanceException(
                String.format("Savings account requires a minimum balance of at least Rs. %.2f", MINIMUM_BALANCE)
            );
        }
    }

    @Override
    public void withdraw(double amount) throws InsufficientBalanceException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero.");
        }
        if (this.balance - amount < MINIMUM_BALANCE) {
            throw new InsufficientBalanceException(
                String.format("Withdrawal denied! Minimum balance of Rs. %.2f must be maintained. Current: Rs. %.2f",
                        MINIMUM_BALANCE, this.balance)
            );
        }
        this.balance -= amount;
    }

    @Override
    public String getAccountType() { return "SAVINGS"; }

    @Override
    public String getAccountSummary() {
        return String.format("Savings Account (Interest: %.1f%%, Min Balance: Rs. %.2f)", interestRate, MINIMUM_BALANCE);
    }
}

class CurrentAccount extends Account {
    private final double overdraftLimit;

    public CurrentAccount(String accountNumber, String customerName, String email, double initialBalance, double overdraftLimit) 
            throws InvalidAmountException {
        super(accountNumber, customerName, email, initialBalance);
        this.overdraftLimit = Math.max(0, overdraftLimit);
    }

    @Override
    public void withdraw(double amount) throws InsufficientBalanceException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero.");
        }
        if (this.balance - amount < -overdraftLimit) {
            double maxAllowed = this.balance + overdraftLimit;
            throw new InsufficientBalanceException(
                String.format("Overdraft limit reached! Maximum allowed withdrawal: Rs. %.2f (Balance: Rs. %.2f, Overdraft: Rs. %.2f)",
                        maxAllowed, this.balance, overdraftLimit)
            );
        }
        this.balance -= amount;
    }

    @Override
    public String getAccountType() { return "CURRENT"; }

    @Override
    public String getAccountSummary() {
        return String.format("Current Account (Overdraft Limit: Rs. %.2f)", overdraftLimit);
    }
}

// =========================================================================
// 4. BANKING SERVICE & REPOSITORIES (Unit 2: Collections Framework)
// =========================================================================

class BankingService {
    // HashMap for fast O(1) account lookup
    private final Map<String, Account> accountMap = new HashMap<>();
    
    // ArrayList for transaction ledger
    private final List<Transaction> transactions = new ArrayList<>();

    private int accountCounter = 1000;
    private int transactionCounter = 1000;

    public String generateAccountNumber() {
        accountCounter++;
        return "ACC" + accountCounter;
    }

    private String generateTransactionId() {
        transactionCounter++;
        return "TXN-" + transactionCounter;
    }

    public Account createAccount(String customerName, String email, String type, double initialBalance, double overdraftLimit) 
            throws BankingException {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new BankingException("Customer name cannot be empty.");
        }
        if (email == null || !email.contains("@")) {
            throw new BankingException("Please provide a valid email address.");
        }

        String accNo = generateAccountNumber();
        Account account;

        if ("CURRENT".equalsIgnoreCase(type)) {
            account = new CurrentAccount(accNo, customerName.trim(), email.trim(), initialBalance, overdraftLimit);
        } else {
            account = new SavingsAccount(accNo, customerName.trim(), email.trim(), initialBalance);
        }

        accountMap.put(accNo, account);

        if (initialBalance > 0) {
            String txnId = generateTransactionId();
            Transaction initialTxn = new Transaction(
                txnId, accNo, TransactionType.DEPOSIT, initialBalance, initialBalance, null, "Account opening balance"
            );
            transactions.add(initialTxn);
        }

        return account;
    }

    public Account getAccount(String accountNumber) throws AccountNotFoundException {
        if (accountNumber == null) {
            throw new AccountNotFoundException("Account number cannot be empty.");
        }
        Account account = accountMap.get(accountNumber.trim().toUpperCase());
        if (account == null) {
            throw new AccountNotFoundException("Account '" + accountNumber + "' was not found.");
        }
        return account;
    }

    public List<Account> getAllAccounts() {
        return new ArrayList<>(accountMap.values());
    }

    public Transaction deposit(String accountNumber, double amount, String description) throws BankingException {
        Account account = getAccount(accountNumber);
        account.deposit(amount);
        String txnId = generateTransactionId();
        String note = (description != null && !description.trim().isEmpty()) ? description : "Cash deposit";
        Transaction txn = new Transaction(txnId, accountNumber, TransactionType.DEPOSIT, amount, account.getBalance(), null, note);
        transactions.add(txn);
        return txn;
    }

    public Transaction withdraw(String accountNumber, double amount, String description) throws BankingException {
        Account account = getAccount(accountNumber);
        account.withdraw(amount);
        String txnId = generateTransactionId();
        String note = (description != null && !description.trim().isEmpty()) ? description : "Cash withdrawal";
        Transaction txn = new Transaction(txnId, accountNumber, TransactionType.WITHDRAWAL, amount, account.getBalance(), null, note);
        transactions.add(txn);
        return txn;
    }

    public List<Transaction> transfer(String fromAccNo, String toAccNo, double amount, String remark) throws BankingException {
        if (fromAccNo == null || toAccNo == null) {
            throw new BankingException("Both source and destination account numbers are required.");
        }
        if (fromAccNo.equalsIgnoreCase(toAccNo)) {
            throw new BankingException("Source and destination accounts cannot be the same.");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than zero.");
        }

        Account source = getAccount(fromAccNo);
        Account target = getAccount(toAccNo);

        // Deduct from source first
        source.withdraw(amount);

        // Credit to target
        try {
            target.deposit(amount);
        } catch (Exception e) {
            // Rollback if target deposit fails
            source.deposit(amount);
            throw new BankingException("Transfer failed during crediting target account: " + e.getMessage());
        }

        String txnIdOut = generateTransactionId();
        String txnIdIn = generateTransactionId();
        String note = (remark != null && !remark.trim().isEmpty()) ? remark : "Fund transfer";

        Transaction debitTxn = new Transaction(txnIdOut, fromAccNo, TransactionType.TRANSFER_OUT, amount, source.getBalance(), toAccNo, "Transfer to " + toAccNo + " (" + note + ")");
        Transaction creditTxn = new Transaction(txnIdIn, toAccNo, TransactionType.TRANSFER_IN, amount, target.getBalance(), fromAccNo, "Transfer from " + fromAccNo + " (" + note + ")");

        transactions.add(debitTxn);
        transactions.add(creditTxn);

        List<Transaction> result = new ArrayList<>();
        result.add(debitTxn);
        result.add(creditTxn);
        return result;
    }

    // Filter and sort using standard loops and Comparator (No Streams)
    public List<Transaction> getTransactions(String accountNumber, String typeFilter, String sortBy) {
        List<Transaction> filtered = new ArrayList<>();
        String targetAcc = (accountNumber == null || accountNumber.trim().isEmpty() || "ALL".equalsIgnoreCase(accountNumber)) ? null : accountNumber.trim().toUpperCase();

        for (Transaction t : transactions) {
            // Check Account Filter
            if (targetAcc != null) {
                boolean matchesSource = t.getAccountNumber().equalsIgnoreCase(targetAcc);
                boolean matchesTarget = t.getTargetAccountNumber() != null && t.getTargetAccountNumber().equalsIgnoreCase(targetAcc);
                if (!matchesSource && !matchesTarget) {
                    continue;
                }
            }

            // Check Type Filter
            if (typeFilter != null && !typeFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(typeFilter)) {
                if (!t.getType().name().equalsIgnoreCase(typeFilter)) {
                    continue;
                }
            }

            filtered.add(t);
        }

        // Sorting using custom Comparator
        Collections.sort(filtered, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                if ("AMOUNT_DESC".equalsIgnoreCase(sortBy)) {
                    return Double.compare(t2.getAmount(), t1.getAmount());
                } else if ("AMOUNT_ASC".equalsIgnoreCase(sortBy)) {
                    return Double.compare(t1.getAmount(), t2.getAmount());
                } else if ("DATE_ASC".equalsIgnoreCase(sortBy)) {
                    return t1.getTimestamp().compareTo(t2.getTimestamp());
                } else {
                    // DATE_DESC (newest first)
                    return t2.getTimestamp().compareTo(t1.getTimestamp());
                }
            }
        });

        return filtered;
    }

    // Calculate total bank summary using simple for-each loops
    public void printBankAnalytics() {
        double totalBalance = 0.0;
        int savings = 0;
        int current = 0;

        for (Account a : accountMap.values()) {
            totalBalance += a.getBalance();
            if ("SAVINGS".equalsIgnoreCase(a.getAccountType())) {
                savings++;
            } else {
                current++;
            }
        }

        double totalAmount = 0.0;
        double maxAmount = 0.0;
        for (Transaction t : transactions) {
            totalAmount += t.getAmount();
            if (t.getAmount() > maxAmount) {
                maxAmount = t.getAmount();
            }
        }
        double avgAmount = transactions.isEmpty() ? 0.0 : (totalAmount / transactions.size());

        System.out.println("\n----------------- BANK ANALYTICS -----------------");
        System.out.println(" Total Accounts Registered : " + accountMap.size());
        System.out.println("   - Savings Accounts       : " + savings);
        System.out.println("   - Current Accounts       : " + current);
        System.out.println(" Total Bank Cash Reserves  : Rs. " + String.format("%.2f", totalBalance));
        System.out.println(" Total Transactions Executed: " + transactions.size());
        System.out.println(" Average Transaction Amount : Rs. " + String.format("%.2f", avgAmount));
        System.out.println(" Highest Transaction Amount : Rs. " + String.format("%.2f", maxAmount));
        System.out.println("--------------------------------------------------");
    }
}

// =========================================================================
// 5. MAIN ENTRY POINT & CONSOLE CLI (Scanner User Interaction)
// =========================================================================

public class BankingApplication {
    private static final Scanner scanner = new Scanner(System.in);
    private static final BankingService bankingService = new BankingService();

    public static void main(String[] args) {
        // Seed initial demo data for immediate testing
        try {
            Account acc1 = bankingService.createAccount("Aarav Sharma", "aarav@example.com", "SAVINGS", 25000.0, 0);
            Account acc2 = bankingService.createAccount("Diya Patel", "diya@example.com", "SAVINGS", 15500.0, 0);
            bankingService.createAccount("Rohan Verma", "rohan@example.com", "CURRENT", 50000.0, 25000.0);
            
            bankingService.deposit(acc1.getAccountNumber(), 5000.0, "Salary bonus");
            bankingService.transfer(acc1.getAccountNumber(), acc2.getAccountNumber(), 3500.0, "Rent contribution");
        } catch (Exception e) {
            System.err.println("Seed error: " + e.getMessage());
        }

        System.out.println("===============================================================");
        System.out.println("             BANKING TRANSACTION SIMULATOR                     ");
        System.out.println("       (Core OOP, Custom Exceptions & Java Collections)        ");
        System.out.println("===============================================================");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Select an option (1-8): ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        handleCreateAccount();
                        break;
                    case "2":
                        handleDeposit();
                        break;
                    case "3":
                        handleWithdrawal();
                        break;
                    case "4":
                        handleTransfer();
                        break;
                    case "5":
                        handleBalanceEnquiry();
                        break;
                    case "6":
                        handleTransactionHistory();
                        break;
                    case "7":
                        bankingService.printBankAnalytics();
                        break;
                    case "8":
                        running = false;
                        System.out.println("\nExiting application. Thank you!");
                        break;
                    default:
                        System.out.println("\n[!] Invalid selection. Please enter a number between 1 and 8.");
                }
            } catch (BankingException e) {
                System.out.println("\n[BANKING ERROR] " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("\n[INPUT ERROR] Please enter a valid numerical value.");
            } catch (Exception e) {
                System.out.println("\n[UNEXPECTED ERROR] " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n----------------- MAIN MENU -----------------");
        System.out.println(" 1. Create New Account (Savings / Current)");
        System.out.println(" 2. Deposit Money");
        System.out.println(" 3. Withdraw Money");
        System.out.println(" 4. Inter-Account Fund Transfer");
        System.out.println(" 5. Balance Enquiry & Account Details");
        System.out.println(" 6. Transaction History / Ledger");
        System.out.println(" 7. Bank Analytics & Total Reserves");
        System.out.println(" 8. Exit");
        System.out.println("---------------------------------------------");
    }

    private static void handleCreateAccount() throws BankingException {
        System.out.println("\n--- Create New Account ---");
        System.out.print("Enter Customer Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();
        System.out.print("Select Account Type (1 for SAVINGS, 2 for CURRENT): ");
        String typeChoice = scanner.nextLine().trim();
        String type = "2".equals(typeChoice) ? "CURRENT" : "SAVINGS";

        System.out.print("Enter Initial Deposit (Rs.): ");
        double balance = Double.parseDouble(scanner.nextLine().trim());

        double overdraft = 0;
        if ("CURRENT".equals(type)) {
            System.out.print("Enter Overdraft Limit [Press Enter for default Rs. 25,000]: ");
            String odStr = scanner.nextLine().trim();
            overdraft = odStr.isEmpty() ? 25000.0 : Double.parseDouble(odStr);
        }

        Account acc = bankingService.createAccount(name, email, type, balance, overdraft);
        System.out.println("\n[SUCCESS] Account Created Successfully!");
        System.out.println(" Account Number: " + acc.getAccountNumber());
        System.out.println(" Account Holder: " + acc.getCustomerName());
        System.out.println(" Type          : " + acc.getAccountType());
        System.out.println(" Balance       : Rs. " + String.format("%.2f", acc.getBalance()));
        System.out.println(" Rule Info     : " + acc.getAccountSummary());
    }

    private static void handleDeposit() throws BankingException {
        System.out.println("\n--- Deposit Funds ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();
        System.out.print("Enter Deposit Amount (Rs.): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Remarks/Description (optional): ");
        String note = scanner.nextLine().trim();

        Transaction txn = bankingService.deposit(accNo, amount, note);
        System.out.println("\n[SUCCESS] Deposit processed successfully!");
        System.out.println(" Transaction ID : " + txn.getTransactionId());
        System.out.println(" Updated Balance: Rs. " + String.format("%.2f", txn.getBalanceAfter()));
    }

    private static void handleWithdrawal() throws BankingException {
        System.out.println("\n--- Withdraw Funds ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();
        System.out.print("Enter Withdrawal Amount (Rs.): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Remarks/Description (optional): ");
        String note = scanner.nextLine().trim();

        Transaction txn = bankingService.withdraw(accNo, amount, note);
        System.out.println("\n[SUCCESS] Withdrawal processed successfully!");
        System.out.println(" Transaction ID  : " + txn.getTransactionId());
        System.out.println(" Remaining Balance: Rs. " + String.format("%.2f", txn.getBalanceAfter()));
    }

    private static void handleTransfer() throws BankingException {
        System.out.println("\n--- Inter-Account Fund Transfer ---");
        System.out.print("Source Account Number      : ");
        String fromAcc = scanner.nextLine().trim();
        System.out.print("Destination Account Number : ");
        String toAcc = scanner.nextLine().trim();
        System.out.print("Transfer Amount (Rs.)      : ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Transfer Remarks           : ");
        String note = scanner.nextLine().trim();

        List<Transaction> txns = bankingService.transfer(fromAcc, toAcc, amount, note);
        System.out.println("\n[SUCCESS] Transfer Completed!");
        System.out.println(" Debit Txn  : " + txns.get(0).getTransactionId() + " (Source Balance: Rs. " + String.format("%.2f", txns.get(0).getBalanceAfter()) + ")");
        System.out.println(" Credit Txn : " + txns.get(1).getTransactionId() + " (Target Balance: Rs. " + String.format("%.2f", txns.get(1).getBalanceAfter()) + ")");
    }

    private static void handleBalanceEnquiry() throws BankingException {
        System.out.println("\n--- Account Details & Balance ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();

        Account acc = bankingService.getAccount(accNo);
        System.out.println("\n==================================");
        System.out.println(" Account Number: " + acc.getAccountNumber());
        System.out.println(" Holder Name   : " + acc.getCustomerName());
        System.out.println(" Email Address : " + acc.getEmail());
        System.out.println(" Account Type  : " + acc.getAccountType());
        System.out.println(" Balance       : Rs. " + String.format("%.2f", acc.getBalance()));
        System.out.println(" Account Info  : " + acc.getAccountSummary());
        System.out.println("==================================");
    }

    private static void handleTransactionHistory() {
        System.out.println("\n--- Transaction History / Ledger ---");
        System.out.print("Enter Account Number (or press Enter for ALL): ");
        String accNo = scanner.nextLine().trim();
        if (accNo.isEmpty()) accNo = "ALL";

        System.out.print("Filter by Type (DEPOSIT/WITHDRAWAL/TRANSFER_IN/TRANSFER_OUT or Enter for ALL): ");
        String type = scanner.nextLine().trim();
        if (type.isEmpty()) type = "ALL";

        System.out.print("Sort By (1: Newest First, 2: Oldest First, 3: Amount High-to-Low): ");
        String sortChoice = scanner.nextLine().trim();
        String sortBy = "DATE_DESC";
        if ("2".equals(sortChoice)) sortBy = "DATE_ASC";
        else if ("3".equals(sortChoice)) sortBy = "AMOUNT_DESC";

        List<Transaction> list = bankingService.getTransactions(accNo, type, sortBy);
        System.out.println("\nTransactions Found: " + list.size());
        System.out.println("--------------------------------------------------------------------------------------------------");
        for (Transaction t : list) {
            System.out.println(" " + t);
        }
        System.out.println("--------------------------------------------------------------------------------------------------");
    }
}
