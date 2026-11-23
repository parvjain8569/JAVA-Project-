public class SavingsAccount extends Account {
    double minBal = 1000.0;

    public SavingsAccount(String accNo, String name, String email, String pin, double balance) 
            throws InvalidAmountException, InsufficientBalanceException {
        super(accNo, name, email, pin, balance);
        if (balance < minBal) {
            throw new InsufficientBalanceException("Opening balance must be at least Rs 1000 for Savings");
        }
    }

    public void withdraw(double amt) throws InsufficientBalanceException, InvalidAmountException {
        if (amt <= 0) {
            throw new InvalidAmountException("Enter valid amount");
        }
        if (balance - amt < minBal) {
            throw new InsufficientBalanceException("Cannot withdraw! Minimum balance of Rs 1000 required");
        }
        balance = balance - amt;
        System.out.println("Amount withdrawn: Rs " + amt);
        System.out.println("Remaining balance: Rs " + balance);
    }

    public String getAccountType() {
        return "SAVINGS";
    }

    public void display() {
        super.display();
        System.out.println("Account Type   : Savings Account");
        System.out.println("Min Balance    : Rs " + minBal);
    }
}
