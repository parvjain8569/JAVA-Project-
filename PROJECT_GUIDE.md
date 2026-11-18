# Banking Transaction Simulator - Project Documentation & Code Walkthrough

This document provides an in-depth, viva-oriented explanation of the entire project, its architecture, syllabus alignment, classes, methods, and execution flow.

---

## 1. Syllabus & Academic Alignment (Chitkara University)

This project is tailored strictly to the concepts taught in **Lectures 1 to 25** of **25CSE0202 (Programming in Java)**:

* **Unit 1: Object-Oriented Programming (OOP) & Exception Handling**
  * **Encapsulation**: Private fields (`balance`, `accountNumber`) with getter methods and safe mutators.
  * **Abstraction**: Abstract base class `Account` with abstract methods (`withdraw`, `getAccountType`, `getAccountSummary`).
  * **Inheritance**: Subclasses `SavingsAccount` and `CurrentAccount` extending `Account`.
  * **Polymorphism**: Dynamic method dispatch where `withdraw()` behaves differently for Savings (minimum balance rule) and Current (overdraft rule).
  * **Custom Exception Handling**: Custom hierarchy (`BankingException`, `InsufficientBalanceException`, `InvalidAmountException`, `AccountNotFoundException`) handled with `try-catch-finally`, `throw`, and `throws`.
* **Unit 2: Java Collections Framework**
  * **`HashMap<String, Account>`**: Fast $O(1)$ key-value account lookup by account number.
  * **`ArrayList<Transaction>`**: Dynamic ordered list maintaining the entire ledger history.
  * **Custom `Comparator<Transaction>`**: Traditional anonymous class implementation to sort transactions by timestamp or amount.
  * **Standard Loops**: Plain `for-each` loops and conditional logic (no Java 8 Streams, no Lambdas, no Threads, no Database).

---

## 2. Project File Structure

The project has been unified into clean, self-contained files:

```text
Java/project/
│
├── BankingApplication.java   # Complete Java source code (all classes & main method)
├── PROJECT_GUIDE.md          # Comprehensive explanation and viva preparation guide (this file)
├── README.md                 # Project summary and quick start
└── .gitignore                # Git ignore rules for compiled binaries
```

---

## 3. Detailed Component & Class Breakdown

All components are cleanly organized inside [BankingApplication.java](file:///c:/Users/parvj/OneDrive/Desktop/FOLDERS/sem%203/Java/project/BankingApplication.java):

### Section A: Custom Exceptions (Unit 1)
| Class Name | Superclass | Purpose | When It Is Thrown |
| :--- | :--- | :--- | :--- |
| `BankingException` | `Exception` | Base checked exception for the banking domain. | General domain errors (e.g., empty customer name, same source/destination account). |
| `InsufficientBalanceException` | `BankingException` | Handles balance violations. | When a Savings account breaches the Rs. 1,000 minimum balance, or Current account exceeds its overdraft limit. |
| `InvalidAmountException` | `BankingException` | Handles input value validation. | When deposit, withdrawal, or transfer amount is $\le 0$. |
| `AccountNotFoundException` | `BankingException` | Handles missing accounts. | When a user looks up an account number not present in the `HashMap`. |

---

### Section B: Transaction Model & Enum (Unit 1)
* **`enum TransactionType`**:
  * Values: `DEPOSIT`, `WITHDRAWAL`, `TRANSFER_IN`, `TRANSFER_OUT`.
  * Enforces type safety so invalid transaction categories cannot be assigned.
* **`class Transaction`**:
  * Fields: `transactionId`, `accountNumber`, `type`, `amount`, `balanceAfter`, `targetAccountNumber`, `description`, `timestamp`.
  * Formatted output in `toString()`: Uses `LocalDateTime` and `DateTimeFormatter` to output clear tabular mini-statement entries.

---

### Section C: OOP Account Hierarchy (Unit 1)

#### 1. `abstract class Account`
* **Fields**:
  * `private final String accountNumber`: Unique ID (e.g., `ACC1001`).
  * `private final String customerName`: Account holder's name.
  * `private final String email`: Contact email.
  * `protected double balance`: Accessible by child classes (`SavingsAccount`, `CurrentAccount`).
  * `private final LocalDateTime createdAt`: Account opening timestamp.
* **Methods**:
  * `deposit(double amount)`: Common logic for all accounts. Validates `amount > 0` and increments `balance`.
  * `abstract void withdraw(double amount)`: Abstract method forcing each child class to implement its own business rule.
  * `abstract String getAccountType()`: Returns `"SAVINGS"` or `"CURRENT"`.
  * `abstract String getAccountSummary()`: Returns policy details (interest rate, minimum balance, or overdraft limit).

#### 2. `class SavingsAccount extends Account`
* **Specific Rules**:
  * `MINIMUM_BALANCE = 1000.00`: Enforces that the balance cannot drop below Rs. 1,000.
  * `interestRate = 4.00`: 4.0% per annum savings interest.
* **Polymorphic `withdraw(double amount)`**:
  * Checks `(this.balance - amount < MINIMUM_BALANCE)`.
  * Throws `InsufficientBalanceException` if minimum balance would be violated.

#### 3. `class CurrentAccount extends Account`
* **Specific Rules**:
  * `overdraftLimit`: Commercial limit (default Rs. 25,000.00) allowing businesses to withdraw even when balance reaches zero.
* **Polymorphic `withdraw(double amount)`**:
  * Checks `(this.balance - amount < -overdraftLimit)`.
  * Allows negative balance up to `-overdraftLimit`.
  * Throws `InsufficientBalanceException` only if the requested amount exceeds `balance + overdraftLimit`.

---

### Section D: Service & Collections Management (Unit 2)

#### `class BankingService`
The business logic orchestrator that manages account operations in memory:

* **Collections Used**:
  * `Map<String, Account> accountMap = new HashMap<>()`:
    * Used for storing and retrieving accounts.
    * Offers average $O(1)$ constant time lookup via `accountMap.get(accountNumber)`.
  * `List<Transaction> transactions = new ArrayList<>()`:
    * Used as an append-only ledger history.
* **Key Methods**:
  * `createAccount(...)`: Validates input, generates `ACC100x`, creates the polymorphic object, saves to `accountMap`, and adds an initial transaction if opening balance > 0.
  * `deposit(...)`: Finds account, calls `account.deposit()`, generates transaction receipt, and records it in the `ArrayList`.
  * `withdraw(...)`: Finds account, polymorphically calls `account.withdraw()`, and records transaction.
  * `transfer(fromAcc, toAcc, amount, remark)`:
    1. Withdraws from source account.
    2. Credits destination account.
    3. If crediting fails, rolls back source balance and throws `BankingException`.
    4. Creates paired debit (`TRANSFER_OUT`) and credit (`TRANSFER_IN`) records.
  * `getTransactions(...)`: Filters records using simple `for` loops (by account number and transaction type) and sorts them using an anonymous `Comparator<Transaction>` (by date or amount).
  * `printBankAnalytics()`: Iterates through `accountMap.values()` and `transactions` to calculate total bank cash reserves, account counts, and average/max transaction values.

---

### Section E: Interactive Terminal Interface (Main Entry Point)

#### `public class BankingApplication`
Contains the `main(String[] args)` method and the `Scanner` console interaction loop:
* **Menu Options**:
  1. **Create New Account**: Prompts for customer name, email, account type (Savings/Current), initial deposit, and overdraft limit.
  2. **Deposit Money**: Prompts for account number and deposit amount.
  3. **Withdraw Money**: Prompts for account number and withdrawal amount.
  4. **Inter-Account Fund Transfer**: Prompts for sender account, recipient account, and amount.
  5. **Balance Enquiry & Account Details**: Shows account summary, balance, and account rules.
  6. **Transaction History / Ledger**: Mini-statement filterable by account or transaction type, with sorting options.
  7. **Bank Analytics & Total Reserves**: Displays total bank reserves and metrics.
  8. **Exit**: Gracefully exits the application.

---

## 4. How to Compile & Run the Project

Open your command prompt or terminal in `c:\Users\parvj\OneDrive\Desktop\FOLDERS\sem 3\Java\project\` and run:

```bash
# Step 1: Compile
javac BankingApplication.java

# Step 2: Run
java BankingApplication
```

---

## 5. Typical Viva Questions & Answers

**Q1: Why did you use an `abstract class` for `Account` instead of an `interface`?**
> *Answer*: `Account` has shared state (`accountNumber`, `balance`, `customerName`) and common implemented behavior (`deposit()`, getters). Interfaces cannot hold instance state. Abstract classes allow sharing both common implementation and declaring abstract methods (`withdraw()`) that subclasses must customize.

**Q2: How is Polymorphism demonstrated here?**
> *Answer*: Through method overriding. Both `SavingsAccount` and `CurrentAccount` override the abstract `withdraw(double amount)` method of `Account`. At runtime, when `account.withdraw()` is called, Java dynamically invokes the appropriate logic based on the actual object type (minimum balance check for Savings vs overdraft check for Current).

**Q3: Why choose `HashMap` for accounts and `ArrayList` for transactions?**
> *Answer*:
> * `HashMap` gives $O(1)$ fast lookups using the unique `accountNumber` as the key.
> * `ArrayList` maintains the sequential order of transactions as they occur, making it ideal for chronological ledger history and iterating for mini-statements.

**Q4: How are transactions sorted without Streams or Lambdas?**
> *Answer*: Using `Collections.sort()` with an anonymous inner class implementing the `Comparator<Transaction>` interface, comparing dates or amounts inside the overridden `compare(t1, t2)` method.
