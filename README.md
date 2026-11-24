# ApexBank — Bank Transfer Simulator

A student-level Java web application that simulates everyday banking tasks. The browser interface is built with HTML, CSS and JavaScript. Core banking rules run in Java, and an optional MySQL layer uses JDBC to save accounts and transactions between restarts.

## Features

- Customer account registration and login
- Savings and current accounts with different withdrawal rules
- Deposit and PIN-authorized withdrawal
- Account-to-account transfer with a confirmation prompt
- Saved beneficiaries (stored in the browser for each account)
- Passbook with account-scoped transaction lookup
- Downloadable CSV statement
- Forgot/reset PIN flow using the registered email
- Optional MySQL persistence through JDBC
- Responsive ApexBank interface with a consistent navy, blue and off-white design

> This is an academic simulator. It does not connect to a real bank or move real money. Do not use real customer details or real banking PINs.

## Requirements

- JDK 8 or newer
- Maven 3.8+ (recommended)
- MySQL 8 (only if you want persistent database storage)

## Run in demo mode (no MySQL needed)

1. Open this project folder in VS Code.
2. Open Terminal in the project folder.
3. Run `mvn compile exec:java` (Windows users can double-click `start_backend.bat`). If you are running demo mode and do not want to use Maven, run `javac *.java` followed by `java Main`.
4. Open `http://localhost:8080` in your browser.
5. Create an account and try the banking features.

Demo mode stores data in memory. Accounts and transactions are cleared when the Java server stops.

## Enable MySQL + JDBC persistence

1. Start MySQL.
2. Open MySQL Workbench or the MySQL command line and run `database/schema.sql`.
3. Copy `db.properties.example` to a new file named `db.properties` in the project root.
4. Update the username and password in `db.properties`.
5. Keep `database.enabled=true`.
6. Run `mvn compile exec:java` again.

The MySQL Connector/J dependency is declared in `pom.xml`; Maven downloads it automatically. On startup, ApexBank loads existing accounts and transactions. After account creation, deposits, withdrawals, transfers and PIN changes, the updated data is saved through JDBC.

The current classroom persistence layer saves the in-memory account and transaction collections together in a database transaction. It is designed for a single-user demonstration, not simultaneous production banking traffic.

## Optional port change

The default port is `8080`. If it is already in use, run:

```bash
mvn compile exec:java -Dexec.args="8091"
```

Then open `http://localhost:8091`.

## Test flow

1. Create a Savings account with an opening balance of at least ₹1,000.
2. Create a second account in another browser session or after signing out.
3. Sign in to the first account.
4. Add the second account under Beneficiaries.
5. Transfer an amount to the second account and confirm the prompt.
6. Open Passbook and download the CSV statement.
7. Try a withdrawal that would take a Savings account below ₹1,000; the Java backend should reject it.

## Project structure

```text
ApexBank/
├── Account.java                 # Abstract account class
├── SavingsAccount.java          # Savings withdrawal rule
├── CurrentAccount.java          # Current account overdraft rule
├── Transaction.java             # Transaction model
├── BankingException.java        # Custom exception classes
├── BankManager.java             # Banking operations and collections
├── DatabaseManager.java         # JDBC persistence (optional MySQL mode)
├── SimpleHttpServer.java        # JDK HTTP server and API handlers
├── Main.java                    # Application entry point and console menu
├── pom.xml                      # Maven + MySQL Connector/J dependency
├── db.properties.example        # MySQL configuration template
├── database/schema.sql          # MySQL tables
├── frontend/
│   ├── index.html               # ApexBank pages and forms
│   ├── style.css                # Shared responsive design
│   └── script.js                # Frontend actions and API calls
├── start_backend.bat
└── start_backend.sh
```

## Important academic notes

- `Account` is an abstract class. `SavingsAccount` and `CurrentAccount` inherit from it.
- `withdraw()` is overridden to demonstrate runtime polymorphism.
- `BankManager` uses `HashMap` for account lookup and `ArrayList` for the transaction passbook.
- Custom exceptions communicate invalid amounts and insufficient balance.
- `DatabaseManager` uses JDBC `Connection`, `Statement`, `PreparedStatement` and SQL transactions.
- `SimpleHttpServer` uses the JDK HTTP server to serve the website and handle JSON requests.

## Current project boundaries

- Beneficiaries are stored in browser `localStorage`, not in MySQL.
- The application is a classroom simulation, not production banking software. It does not implement a full customer session system, production-grade PIN hashing, multi-factor authentication, or concurrent multi-user transaction locking.
- Use only test accounts and sample data during demonstrations.
