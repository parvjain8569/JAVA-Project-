import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/** Optional MySQL persistence layer. Configure db.properties to enable it. */
public class DatabaseManager {
    private String url;
    private String username;
    private String password;
    private boolean enabled;

    public DatabaseManager() {
        Properties properties = new Properties();
        try {
            FileInputStream input = new FileInputStream("db.properties");
            properties.load(input);
            input.close();
            enabled = Boolean.parseBoolean(properties.getProperty("database.enabled", "false"));
            url = properties.getProperty("database.url", "");
            username = properties.getProperty("database.username", "");
            password = properties.getProperty("database.password", "");
        } catch (Exception e) {
            enabled = false;
        }
    }

    public boolean isEnabled() { return enabled; }

    private Connection connect() throws Exception {
        if (!enabled) return null;
        return DriverManager.getConnection(url, username, password);
    }

    public void load(BankManager bank) throws Exception {
        if (!enabled) return;
        Connection connection = connect();
        try {
            createTables(connection);
            bank.map.clear();
            bank.txns.clear();
            int highestAccount = 100;
            int highestTransaction = 0;

            Statement accountStatement = connection.createStatement();
            ResultSet accounts = accountStatement.executeQuery("SELECT account_no, customer_name, email, pin, account_type, balance, overdraft_limit FROM accounts ORDER BY account_no");
            while (accounts.next()) {
                String accountNo = accounts.getString("account_no");
                String type = accounts.getString("account_type");
                Account account;
                if ("CURRENT".equalsIgnoreCase(type)) {
                    account = new CurrentAccount(accountNo, accounts.getString("customer_name"), accounts.getString("email"), accounts.getString("pin"), accounts.getDouble("balance"), accounts.getDouble("overdraft_limit"));
                } else {
                    account = new SavingsAccount(accountNo, accounts.getString("customer_name"), accounts.getString("email"), accounts.getString("pin"), accounts.getDouble("balance"));
                }
                bank.map.put(accountNo, account);
                try {
                    highestAccount = Math.max(highestAccount, Integer.parseInt(accountNo.replaceAll("[^0-9]", "")));
                } catch (Exception ignored) { }
            }
            accounts.close();
            accountStatement.close();

            Statement transactionStatement = connection.createStatement();
            ResultSet transactions = transactionStatement.executeQuery("SELECT id, account_no, type, amount, remaining_balance, remark FROM transactions ORDER BY id");
            while (transactions.next()) {
                int id = transactions.getInt("id");
                bank.txns.add(new Transaction(id, transactions.getString("account_no"), transactions.getString("type"), transactions.getDouble("amount"), transactions.getDouble("remaining_balance"), transactions.getString("remark")));
                highestTransaction = Math.max(highestTransaction, id);
            }
            transactions.close();
            transactionStatement.close();
            bank.accCount = highestAccount + 1;
            bank.txnCount = highestTransaction + 1;
        } finally {
            connection.close();
        }
    }

    private void createTables(Connection connection) throws Exception {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (account_no VARCHAR(20) PRIMARY KEY, customer_name VARCHAR(100) NOT NULL, email VARCHAR(150) NOT NULL UNIQUE, pin VARCHAR(20) NOT NULL, account_type VARCHAR(20) NOT NULL, balance DECIMAL(14,2) NOT NULL, overdraft_limit DECIMAL(14,2) NOT NULL DEFAULT 0)");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (id INT PRIMARY KEY, account_no VARCHAR(20) NOT NULL, type VARCHAR(30) NOT NULL, amount DECIMAL(14,2) NOT NULL, remaining_balance DECIMAL(14,2) NOT NULL, remark VARCHAR(255), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        statement.close();
    }

    /** Saves the in-memory collections as one database transaction for this single-user classroom simulator. */
    public void save(BankManager bank) throws Exception {
        if (!enabled) return;
        Connection connection = connect();
        try {
            createTables(connection);
            connection.setAutoCommit(false);
            Statement delete = connection.createStatement();
            delete.executeUpdate("DELETE FROM transactions");
            delete.executeUpdate("DELETE FROM accounts");
            delete.close();

            PreparedStatement accountInsert = connection.prepareStatement("INSERT INTO accounts(account_no, customer_name, email, pin, account_type, balance, overdraft_limit) VALUES(?,?,?,?,?,?,?)");
            for (Account account : bank.map.values()) {
                accountInsert.setString(1, account.accNo);
                accountInsert.setString(2, account.name);
                accountInsert.setString(3, account.email);
                accountInsert.setString(4, account.pin);
                accountInsert.setString(5, account.getAccountType());
                accountInsert.setDouble(6, account.balance);
                accountInsert.setDouble(7, account instanceof CurrentAccount ? ((CurrentAccount) account).limit : 0);
                accountInsert.addBatch();
            }
            accountInsert.executeBatch();
            accountInsert.close();

            PreparedStatement transactionInsert = connection.prepareStatement("INSERT INTO transactions(id, account_no, type, amount, remaining_balance, remark) VALUES(?,?,?,?,?,?)");
            for (Transaction transaction : bank.txns) {
                transactionInsert.setInt(1, transaction.id);
                transactionInsert.setString(2, transaction.accNo);
                transactionInsert.setString(3, transaction.type);
                transactionInsert.setDouble(4, transaction.amount);
                transactionInsert.setDouble(5, transaction.remBal);
                transactionInsert.setString(6, transaction.remark);
                transactionInsert.addBatch();
            }
            transactionInsert.executeBatch();
            transactionInsert.close();
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.close();
        }
    }
}
