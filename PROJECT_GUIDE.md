# ApexBank — Project Guide and Viva Notes

## 1. Project description

ApexBank is a Java-based Bank Transfer Simulator website. It demonstrates how a simple banking application can register customers, maintain savings/current accounts, accept deposits, process withdrawals, transfer funds between accounts and maintain a transaction passbook. The project is intended for academic demonstration; it does not connect to real banking networks or transfer real money.

## 2. Technology stack

| Layer | Technology | Purpose |
|---|---|---|
| Frontend | HTML | Page structure and forms |
| Styling | CSS | Layout, colours and responsive styling |
| Frontend behaviour | JavaScript | Form submission and API calls |
| Backend | Core Java | Account rules and banking operations |
| Web layer | JDK `HttpServer` | Serves the website and API endpoints |
| Database connectivity | JDBC | Java-to-MySQL communication |
| Database | MySQL | Optional persistent account and transaction storage |

## 3. Main modules

1. **Customer access:** create an account, sign in and reset a PIN using registered email.
2. **Account management:** Savings and Current accounts, account details and balance.
3. **Deposit:** add funds to a selected simulated account.
4. **Withdrawal:** PIN-authorized withdrawal with account-specific balance rules.
5. **Money transfer:** transfer between two accounts after a confirmation prompt and PIN verification.
6. **Beneficiaries:** save frequently used recipient accounts in the browser.
7. **Passbook:** view the logged-in account's transactions.
8. **Statements:** download the visible account transaction history as CSV.

## 4. Java class design

### `Account.java`
Abstract parent class containing account number, customer name, email, PIN and balance. It defines common methods such as `deposit()`, `verifyPin()` and `display()`, and declares abstract methods `withdraw()` and `getAccountType()`.

### `SavingsAccount.java`
Extends `Account`. It maintains a minimum balance of ₹1,000. A withdrawal is rejected if it would take the account below this minimum.

### `CurrentAccount.java`
Extends `Account`. It supports an overdraft limit. A withdrawal is rejected if it would exceed the configured limit.

### `Transaction.java`
Stores transaction ID, account number, transaction type, amount, remaining balance and remark.

### `BankingException.java`
Defines custom checked exceptions for insufficient balance, invalid amount and account not found conditions.

### `BankManager.java`
Contains the main banking logic. It uses a `HashMap<String, Account>` for account lookup and an `ArrayList<Transaction>` for passbook entries. It validates account operations and coordinates transfers.

### `DatabaseManager.java`
Optional JDBC persistence class. If `db.properties` enables MySQL, it loads saved account and transaction rows at startup and saves updated collections using prepared SQL statements and a database transaction.

### `SimpleHttpServer.java`
Uses `com.sun.net.httpserver.HttpServer` from the JDK. It serves files from `frontend/` and exposes endpoints for account creation, login, PIN reset, deposit, withdrawal, transfer, transactions and summary reporting.

### `Main.java`
Starts the web server and provides a simple console menu for testing Java methods.

## 5. Important API endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/create-account` | Create a customer account |
| POST | `/api/login` | Verify account number and PIN |
| POST | `/api/reset-pin` | Reset PIN after email check |
| POST | `/api/deposit` | Deposit funds |
| POST | `/api/withdraw` | Withdraw funds after PIN check |
| POST | `/api/transfer` | Transfer between accounts |
| GET | `/api/transactions?accNo=...&pin=...` | Return passbook rows for the verified account |
| GET | `/api/report` | Return a basic bank summary |

## 6. Database design

### `accounts`

- `account_no` — primary key
- `customer_name` — customer name
- `email` — unique email address
- `pin` — simulator PIN value
- `account_type` — SAVINGS or CURRENT
- `balance` — current balance
- `overdraft_limit` — current-account overdraft allowance

### `transactions`

- `id` — transaction ID
- `account_no` — account related to the transaction
- `type` — CREDIT, DEPOSIT, WITHDRAW, TRANSFER-IN or TRANSFER-OUT
- `amount` — transaction amount
- `remaining_balance` — balance after the transaction
- `remark` — transaction description
- `created_at` — database timestamp

Run `database/schema.sql` to create the database and tables. Copy `db.properties.example` to `db.properties` and fill in local MySQL credentials to enable JDBC mode.

## 7. OOP and Java concepts demonstrated

- **Abstraction:** `Account` defines common account behaviour without implementing account-specific withdrawal rules.
- **Encapsulation:** account data and operations are grouped within classes (fields are package-visible in this starter implementation; they can be made private with getters/setters as a further improvement).
- **Inheritance:** Savings and Current accounts extend Account.
- **Polymorphism:** the overridden `withdraw()` method applies different rules depending on the actual account object.
- **Collections:** `HashMap` and `ArrayList` manage account and transaction data.
- **Exception handling:** invalid amounts, missing accounts and insufficient balance are reported through exceptions.
- **JDBC:** `Connection`, `Statement`, `PreparedStatement`, `ResultSet`, commit and rollback are used for MySQL persistence.
- **Conditional statements and logical operators:** account validation and transaction rules use `if`, `else` and logical conditions.

## 8. Suggested viva questions

**Q1. Why is Account abstract?**  
Because all account types share common information, but each account type has its own withdrawal rule.

**Q2. What is the difference between SavingsAccount and CurrentAccount?**  
SavingsAccount must maintain a minimum balance. CurrentAccount can use an overdraft limit.

**Q3. Where is runtime polymorphism used?**  
When `withdraw()` is called on an `Account` reference, Java executes the overridden method of the actual SavingsAccount or CurrentAccount object.

**Q4. Why use HashMap?**  
It stores account objects using account numbers as keys, making account lookup straightforward.

**Q5. Why use ArrayList?**  
It stores transaction entries in sequence and can be traversed to show the passbook.

**Q6. What is JDBC?**  
JDBC is the Java API used to connect Java programs to relational databases such as MySQL.

**Q7. Why use PreparedStatement?**  
It separates SQL structure from values and helps avoid SQL injection when user input is passed to a query.

**Q8. What happens when a Savings account withdrawal breaks the minimum-balance rule?**  
The Java backend throws an `InsufficientBalanceException`, and the transaction is rejected.

## 9. Scope and limitations

ApexBank is an educational simulator, not production banking software. The optional JDBC mode is designed for a single-user classroom demonstration. Beneficiaries are stored in browser localStorage. The project does not yet include a production-grade authentication/session system, PIN hashing, admin authorization, or concurrent transaction locking. Never use real customer details or real banking PINs.
## Start the website on Windows (without Maven)

1. Install a Java JDK (the `javac` compiler must be available).
2. Double-click `start_backend.bat`.
3. Keep the black Command Prompt window open.
4. Open Chrome and visit `http://localhost:8080` (do not open `frontend/index.html` directly).
5. To stop the server, press `Ctrl+C` in the Command Prompt window.

The no-Maven launcher compiles the Java source files into the local `out` folder and starts the web-only server. If MySQL is not configured, the app uses in-memory demo data, which resets when the server stops.
