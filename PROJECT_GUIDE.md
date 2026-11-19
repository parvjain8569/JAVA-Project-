# Apex Bank - Digital Banking Transaction Simulator
## Project Documentation & Complete Viva Guide (3rd Semester)

This comprehensive guide covers everything implemented in the project, explaining every class, data structure, syllabus concept (Chitkara University 25CSE0202 - Programming in Java), security mechanism, and likely viva/evaluation questions.

---

## 1. Syllabus & Academic Mapping (Lectures 1 to 25)

The project is strictly crafted to reflect the content taught up to **Unit 2 (before Multithreading/Threads)**, written in authentic student-level Java:

| Syllabus Unit & Topic | Implementation in this Project |
| :--- | :--- |
| **Unit 1: Classes & Encapsulation** | `Account`, `SavingsAccount`, `CurrentAccount`, and `Transaction` classes with fields, constructors, and methods. |
| **Unit 1: Abstraction** | `abstract class Account` defining core state with abstract method `withdraw(double amt)` and `getAccountType()`. |
| **Unit 1: Inheritance** | `SavingsAccount extends Account` and `CurrentAccount extends Account`. Subclasses call `super(...)`. |
| **Unit 1: Polymorphism** | Dynamic method dispatch on `withdraw(double amt)`: Savings enforces minimum balance, while Current implements overdraft facility. |
| **Unit 1: Custom Exception Handling** | Custom checked exceptions (`InsufficientBalanceException`, `InvalidAmountException`, `AccountNotFoundException`) handled with `try-catch`, `throw`, and `throws`. |
| **Unit 2: Java Collections Framework** | • `HashMap<String, Account>`: For $O(1)$ fast account lookups.<br>• `ArrayList<Transaction>`: For recording sequential ledger passbook history.<br>• Anonymous `Comparator<Transaction>`: For sorting transactions by amount without Lambdas/Streams. |
| **Web Server & UI (Full-Stack)** | Built-in JDK `com.sun.net.httpserver.HttpServer` on port `8080` serving a modern monochrome Customer Portal (HTML, CSS, JS) with PIN verification, instant transfers, cash deposit, and passbook. |

---

## 2. Project Directory Structure

```text
Java/project/
│
├── Account.java              # Abstract base class for all accounts
├── SavingsAccount.java       # Savings account logic (Min balance Rs. 1,000)
├── CurrentAccount.java       # Current account logic (Overdraft facility)
├── Transaction.java          # Simple transaction ledger data model
├── BankingException.java      # Custom exceptions (InsufficientBalance, InvalidAmount, etc.)
├── BankManager.java          # Collections manager (HashMap, ArrayList, business logic)
├── SimpleHttpServer.java     # Built-in lightweight HTTP server & REST APIs
├── Main.java                 # Entrypoint: starts web server and console CLI
├── start_backend.bat         # 1-click script to compile and launch the project
├── PROJECT_GUIDE.md          # Complete project and viva documentation (this file)
│
└── frontend/                 # Customer Portal Web UI
    ├── index.html            # User portal: onboarding, virtual debit card, tabs
    ├── style.css             # Modern monochrome dark theme styling
    └── script.js             # Client-side controller communicating via REST fetch()
```

---

## 3. Deep Dive into Every Backend Java File

### 1. `BankingException.java`
Contains domain-specific custom exception classes extending `java.lang.Exception`:
* **`InsufficientBalanceException`**: Thrown when a withdrawal violates the ₹1,000 minimum balance (Savings) or exceeds the overdraft limit (Current).
* **`InvalidAmountException`**: Thrown if deposit, withdrawal, or transfer amounts are $\le 0$ or opening balance is negative.
* **`AccountNotFoundException`**: Thrown when searching for an account number that does not exist in the bank's `HashMap`.

---

### 2. `Account.java` (Abstract Class)
Base class providing foundational banking attributes and behaviors:
* **Fields**:
  * `String accNo`: Unique identifier (e.g. `ACC101`).
  * `String name`: Customer's full name.
  * `String email`: Customer's contact email.
  * `String pin`: 4-digit security PIN used for authorizing withdrawals, logins, and transfers.
  * `double balance`: Current monetary balance.
* **Key Methods**:
  * `verifyPin(String enteredPin)`: Compares entered PIN with the stored PIN.
  * `deposit(double amt)`: Validates that `amt > 0`, increases balance, and prints confirmation.
  * `abstract void withdraw(double amt)`: Abstract method forcing each account subtype to supply its own business logic.
  * `abstract String getAccountType()`: Returns `"SAVINGS"` or `"CURRENT"`.
  * `display()`: Prints formatted account summary.

---

### 3. `SavingsAccount.java` (Inheritance & Polymorphism)
Subclass extending `Account` for retail customers:
* **Rules**:
  * `minBal = 1000.0`: Must always maintain at least ₹1,000.
  * Constructor rejects opening deposits $< 1000$ by throwing `InsufficientBalanceException`.
* **Polymorphic `withdraw(double amt)`**:
  * Verifies `(balance - amt >= minBal)`.
  * If the resulting balance would drop below ₹1,000, rejects the transaction with an `InsufficientBalanceException`.

---

### 4. `CurrentAccount.java` (Inheritance & Polymorphism)
Subclass extending `Account` for businesses:
* **Rules**:
  * `limit`: Overdraft facility (e.g. ₹25,000). Allows the balance to drop below ₹0 into negative values down to `-limit`.
* **Polymorphic `withdraw(double amt)`**:
  * Verifies `(balance - amt >= -limit)`.
  * If the requested withdrawal exceeds the balance plus overdraft limit, throws `InsufficientBalanceException`.

---

### 5. `Transaction.java`
Simple class modeling individual ledger transactions:
* **Fields**:
  * `int id`: Auto-incrementing transaction ID (e.g., `1`, `2`).
  * `String accNo`: Associated account number.
  * `String type`: `"DEPOSIT"`, `"WITHDRAW"`, `"TRANSFER-IN"`, or `"TRANSFER-OUT"`.
  * `double amount`: Value of the transaction.
  * `double remBal`: Account balance remaining immediately after the transaction.
  * `String remark`: Notes/description (e.g. `"Sent to ACC102"`, `"Cash deposited"`).
* **Method**:
  * `printTxn()`: Prints transaction details in a single clean line.

---

### 6. `BankManager.java` (Collections & Core Business Logic)
The engine managing all in-memory operations using the Java Collections Framework:
* **Data Structures**:
  * `HashMap<String, Account> map`: Key-value map using `accNo` as key for $O(1)$ constant time lookup.
  * `ArrayList<Transaction> txns`: Sequential list recording all transactions in chronological order.
* **Core Methods**:
  * `openAccount(name, email, pin, type, bal, limit)`:
    * Checks for duplicate accounts by verifying if the `email` already exists in `map.values()`.
    * Validates that the PIN has at least 4 digits.
    * Generates account number `ACC101`, `ACC102`, etc.
    * Adds an opening balance transaction if $bal > 0$.
  * `login(accNo, pin)`: Verifies account existence and checks PIN validity.
  * `resetPin(accNo, email, newPin)`: Matches the registered email for that account and sets the new PIN.
  * `doDeposit(targetAccNo, amt, depositorName)`: Deposits cash into **any target account** without requiring a password.
  * `doWithdraw(accNo, amt, pin)`: Verifies the security PIN before deducting funds.
  * `doTransfer(from, to, amt, senderPin)`:
    1. Verifies the sender's PIN.
    2. Withdraws `amt` from sender.
    3. Deposits `amt` to recipient.
    4. Performs an automatic rollback if the credit step fails.
    5. Records paired debit (`TRANSFER-OUT`) and credit (`TRANSFER-IN`) transactions in `txns`.
  * `showPassbook(accNo)`: Iterates through `txns` with a `for` loop to display records matching `accNo`.
  * `showSortedTransactions()`: Sorts a copy of `txns` by amount (high-to-low) using `Collections.sort()` and an anonymous `Comparator<Transaction>` class.
  * `showSummary()`: Computes total cash and counts by iterating through `map.values()`.

---

### 7. `SimpleHttpServer.java` (Built-in Web Server & REST API)
Uses standard Java SE `com.sun.net.httpserver.HttpServer` on port `8080`:
* Serves the frontend files (`index.html`, `style.css`, `script.js`).
* Exposes clean JSON REST endpoints:
  * `GET /api/accounts` - Retrieves account list.
  * `POST /api/create-account` - Registers a new account with email uniqueness check.
  * `POST /api/login` - Authenticates user with Account No + PIN.
  * `POST /api/reset-pin` - Resets PIN after email verification.
  * `POST /api/deposit` - Deposits funds into any target account number.
  * `POST /api/withdraw` - Withdraws funds (requires PIN).
  * `POST /api/transfer` - Transfers funds to another account (requires sender PIN).
  * `GET /api/transactions` - Returns all transaction ledger entries.
  * `GET /api/report` - Returns total accounts, reserves, and transaction counts.

---

### 8. `Main.java`
* Entrypoint containing `public static void main(String[] args)`.
* Launches `SimpleHttpServer` on port `8080`.
* Provides a console CLI menu with `Scanner` so the evaluator can test operations in the terminal or in the browser simultaneously.

---

## 4. Frontend & Customer Portal Features

* **Authentication & Onboarding Flow**:
  * Users start on a clean landing screen where they can **Create an Account** or **Login** with their existing account number and PIN.
  * Includes a **"Forgot PIN?"** recovery form verifying registered email.
  * Starts fresh on every launch (no ghost data).
* **Virtual Debit Card**:
  * Displays user name, account type, masked account number (`ACC---`), and live balance.
* **Privacy Mode (Eye Icon 👁️ / 🙈)**:
  * Toggles blurring of balances and sensitive figures.
* **Action Sections**:
  1. **Deposit Money**: Deposit cash into your own account or specify any other target account number.
  2. **Withdraw Money**: ATM withdrawal requiring the 4-digit Security PIN.
  3. **Transfer Funds**: Inter-account bank transfer requiring sender's PIN.
  4. **My Passbook**: Shows all debits, credits, and remaining balance for the logged-in user.
  5. **Saved Beneficiaries**: Save trusted payees with one-click **Transfer**, **Deposit**, or **Delete** actions.

---

## 5. How to Run the Project

1. **Option 1 (One-Click)**:
   Double-click `start_backend.bat`.

2. **Option 2 (Terminal Command)**:
   ```bash
   javac *.java
   java Main
   ```

3. Open your browser:
   **[http://localhost:8080](http://localhost:8080)**

---

## 6. Frequently Asked Viva Questions & Answers

**Q1: Why did you use an `abstract class` for `Account` instead of an `interface`?**
> *Answer*: `Account` holds common state (`accNo`, `name`, `balance`, `pin`) and common logic (`deposit()`, `verifyPin()`). Interfaces cannot hold non-static instance variables. An abstract class allows sharing common fields and concrete methods while leaving `withdraw()` abstract for subclasses to define.

**Q2: Where is Polymorphism used in your project?**
> *Answer*: In method overriding. Both `SavingsAccount` and `CurrentAccount` override the abstract `withdraw(double amt)` method of `Account`. At runtime, when `withdraw()` is invoked on an `Account` reference, Java dynamically executes the subtype's specific version (checking minimum balance for Savings, or overdraft limit for Current).

**Q3: Why choose `HashMap` for storing accounts and `ArrayList` for transactions?**
> *Answer*:
> * `HashMap` provides average $O(1)$ constant time lookup for accounts using the unique `accNo` key.
> * `ArrayList` provides an ordered, index-based dynamic array that preserves the exact chronological sequence in which transactions occur.

**Q4: How do you sort transactions without using Java 8 Streams or Lambdas?**
> *Answer*: By using `Collections.sort()` with an anonymous inner class implementing the `Comparator<Transaction>` interface, comparing `amount` inside the overridden `compare()` method.

**Q5: How does the transfer method prevent money loss?**
> *Answer*: It performs atomic debit and credit operations in sequence. If crediting the destination account encounters an error, the catch block immediately rolls back the debited amount to the source account (`a1.deposit(amt)`) before throwing an exception.
