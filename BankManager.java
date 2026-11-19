import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class BankManager {
    // using hashmap for quick account lookup
    HashMap<String, Account> map = new HashMap<String, Account>();

    // using arraylist to store all transactions
    ArrayList<Transaction> txns = new ArrayList<Transaction>();

    int accCount = 101;
    int txnCount = 1;

    // method to add account with PIN and duplicate email check
    public Account openAccount(String name, String email, String pin, String type, double bal, double limit) throws Exception {
        if (name == null || name.trim().equals("")) {
            throw new Exception("Name cannot be empty!");
        }
        if (email == null || email.trim().equals("")) {
            throw new Exception("Email cannot be empty!");
        }
        if (pin == null || pin.trim().length() < 4) {
            throw new Exception("Security PIN must be at least 4 digits!");
        }

        // check if an account with this email already exists
        for (Account existing : map.values()) {
            if (existing.email.equalsIgnoreCase(email.trim())) {
                throw new Exception("An account with email '" + email + "' already exists! (Account No: " + existing.accNo + ")");
            }
        }

        String accNo = "ACC" + accCount;
        accCount++;

        Account acc;
        if (type.equalsIgnoreCase("current")) {
            acc = new CurrentAccount(accNo, name, email, pin, bal, limit);
        } else {
            acc = new SavingsAccount(accNo, name, email, pin, bal);
        }

        map.put(accNo, acc);

        // initial deposit entry
        if (bal > 0) {
            Transaction t = new Transaction(txnCount++, accNo, "CREDIT", bal, bal, "Opening balance");
            txns.add(t);
        }

        return acc;
    }

    public Account findAccount(String accNo) throws AccountNotFoundException {
        if (accNo == null) {
            throw new AccountNotFoundException("Account number cannot be empty!");
        }
        String clean = accNo.trim().toUpperCase();
        if (map.containsKey(clean)) {
            return map.get(clean);
        } else {
            throw new AccountNotFoundException("Account " + clean + " does not exist!");
        }
    }

    // Login with account number and PIN verification
    public Account login(String accNo, String pin) throws Exception {
        Account a = findAccount(accNo);
        if (!a.verifyPin(pin)) {
            throw new Exception("Incorrect Security PIN / Password! Please try again.");
        }
        return a;
    }

    // Reset PIN using email verification
    public void resetPin(String accNo, String registeredEmail, String newPin) throws Exception {
        Account a = findAccount(accNo);
        if (!a.email.equalsIgnoreCase(registeredEmail.trim())) {
            throw new Exception("Email does not match our records for account " + accNo.trim().toUpperCase() + "!");
        }
        if (newPin == null || newPin.trim().length() < 4) {
            throw new Exception("New Security PIN must be at least 4 digits!");
        }
        a.pin = newPin.trim();
        System.out.println("PIN successfully updated for " + a.accNo);
    }

    // Deposit to ANY account (asks for target account number, no PIN required)
    public void doDeposit(String targetAccNo, double amt, String depositorName) throws Exception {
        Account a = findAccount(targetAccNo);
        a.deposit(amt);
        String note = (depositorName != null && !depositorName.trim().equals("")) 
                      ? "Deposit by " + depositorName 
                      : "Cash deposited";
        Transaction t = new Transaction(txnCount++, targetAccNo, "DEPOSIT", amt, a.balance, note);
        txns.add(t);
    }

    // Withdrawal requires PIN verification
    public void doWithdraw(String accNo, double amt, String enteredPin) throws Exception {
        Account a = findAccount(accNo);
        if (!a.verifyPin(enteredPin)) {
            throw new Exception("Incorrect Security PIN! Withdrawal rejected.");
        }
        a.withdraw(amt);
        Transaction t = new Transaction(txnCount++, accNo, "WITHDRAW", amt, a.balance, "Self withdrawal");
        txns.add(t);
    }

    // Transfer requires sender's PIN verification
    public void doTransfer(String from, String to, double amt, String senderPin) throws Exception {
        if (from.equalsIgnoreCase(to)) {
            throw new Exception("Source and destination account cannot be same!");
        }

        Account a1 = findAccount(from);
        Account a2 = findAccount(to);

        // verify sender's PIN
        if (!a1.verifyPin(senderPin)) {
            throw new Exception("Incorrect Security PIN! Transfer aborted.");
        }

        // withdraw from first
        a1.withdraw(amt);

        // deposit to second
        try {
            a2.deposit(amt);
        } catch (Exception e) {
            // rollback
            a1.deposit(amt);
            throw new Exception("Transfer failed: " + e.getMessage());
        }

        Transaction t1 = new Transaction(txnCount++, from, "TRANSFER-OUT", amt, a1.balance, "Sent to " + to);
        Transaction t2 = new Transaction(txnCount++, to, "TRANSFER-IN", amt, a2.balance, "Received from " + from);

        txns.add(t1);
        txns.add(t2);

        System.out.println("Transfer successful!");
    }

    public void showPassbook(String accNo) {
        System.out.println("\n--- Passbook for " + accNo + " ---");
        boolean found = false;
        for (int i = 0; i < txns.size(); i++) {
            Transaction t = txns.get(i);
            if (t.accNo.equalsIgnoreCase(accNo)) {
                t.printTxn();
                found = true;
            }
        }
        if (!found) {
            System.out.println("No transactions found for this account.");
        }
    }

    // sorting using Comparator interface
    public void showSortedTransactions() {
        if (txns.size() == 0) {
            System.out.println("No transactions to sort.");
            return;
        }

        ArrayList<Transaction> list = new ArrayList<Transaction>(txns);

        // anonymous class for comparator
        Collections.sort(list, new Comparator<Transaction>() {
            public int compare(Transaction o1, Transaction o2) {
                if (o1.amount < o2.amount) {
                    return 1;
                } else if (o1.amount > o2.amount) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        System.out.println("\n--- Transactions sorted by amount (High to Low) ---");
        for (int i = 0; i < list.size(); i++) {
            list.get(i).printTxn();
        }
    }

    public void showSummary() {
        System.out.println("\n--- Bank Report ---");
        System.out.println("Total Accounts: " + map.size());

        double totalCash = 0;
        for (Account a : map.values()) {
            totalCash = totalCash + a.balance;
        }
        System.out.println("Total Cash in Bank: Rs " + totalCash);
        System.out.println("Total Transactions Done: " + txns.size());
    }
}
