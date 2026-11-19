// Custom exceptions for bank project

class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String s) {
        super(s);
    }
}

class InvalidAmountException extends Exception {
    public InvalidAmountException(String s) {
        super(s);
    }
}

class AccountNotFoundException extends Exception {
    public AccountNotFoundException(String s) {
        super(s);
    }
}
