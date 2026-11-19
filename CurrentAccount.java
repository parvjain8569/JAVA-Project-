// Current account for business with overdraft

public class CurrentAccount extends Account {
    double limit; // overdraft limit

    public CurrentAccount(String accNo, String name, String email, String pin, double balance, double limit) 
            throws InvalidAmountException {
        super(accNo, name, email, pin, balance);
        this.limit = limit;
    }

    public void withdraw(double amt) throws InsufficientBalanceException, InvalidAmountException {
        if (amt <= 0) {
            throw new InvalidAmountException("Enter valid amount");
        }
        // checking overdraft
        if (balance - amt < -limit) {
            throw new InsufficientBalanceException("Overdraft limit reached! You can only withdraw up to Rs " + (balance + limit));
        }
        balance = balance - amt;
        System.out.println("Amount withdrawn: Rs " + amt);
        System.out.println("Remaining balance: Rs " + balance);
    }

    public String getAccountType() {
        return "CURRENT";
    }

    public void display() {
        super.display();
        System.out.println("Account Type   : Current Account");
        System.out.println("Overdraft Limit: Rs " + limit);
    }
}
