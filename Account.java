// Base Account class - abstract class

public abstract class Account {
    String accNo;
    String name;
    String email;
    String pin; // 4-digit security PIN for withdrawals and transfers
    double balance;

    public Account(String accNo, String name, String email, String pin, double balance) throws InvalidAmountException {
        if (balance < 0) {
            throw new InvalidAmountException("Balance cannot be negative");
        }
        this.accNo = accNo;
        this.name = name;
        this.email = email;
        this.pin = (pin != null && pin.trim().length() > 0) ? pin.trim() : "1234";
        this.balance = balance;
    }

    public boolean verifyPin(String enteredPin) {
        return this.pin.equals(enteredPin);
    }

    public void deposit(double amt) throws InvalidAmountException {
        if (amt <= 0) {
            throw new InvalidAmountException("Please enter amount greater than 0");
        }
        balance = balance + amt;
        System.out.println("Amount deposited: Rs " + amt);
        System.out.println("New balance: Rs " + balance);
    }

    // abstract method to be overridden
    public abstract void withdraw(double amt) throws InsufficientBalanceException, InvalidAmountException;
    public abstract String getAccountType();

    public void display() {
        System.out.println("Account Number : " + accNo);
        System.out.println("Holder Name    : " + name);
        System.out.println("Email ID       : " + email);
        System.out.println("Current Balance: Rs " + balance);
    }
}
