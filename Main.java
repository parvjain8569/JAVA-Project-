import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        BankManager bank = new BankManager();

        SimpleHttpServer server = new SimpleHttpServer(bank);
        int port = 8080;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) { }
        }
        server.start(port);

        // Web-only mode is used by start_backend.bat so students do not
        // have to interact with the separate console banking menu.
        if (args.length > 1 && "web".equalsIgnoreCase(args[1])) {
            System.out.println("ApexBank is ready. Open http://localhost:" + port + " in your browser.");
            try {
                new CountDownLatch(1).await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return;
        }

        int ch = 0;

        while (ch != 8) {
            System.out.println("\n*** APEXBANK SIMULATOR MENU ***");
            System.out.println("1. Open Account");
            System.out.println("2. Deposit Money (Enter Any Account No)");
            System.out.println("3. Withdraw Money (Requires PIN)");
            System.out.println("4. Transfer Money (Requires PIN)");
            System.out.println("5. Check Account Balance");
            System.out.println("6. Account Passbook / Transactions");
            System.out.println("7. Total Bank Cash Report");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");

            try {
                ch = sc.nextInt();
                sc.nextLine();

                switch (ch) {
                    case 1:
                        System.out.print("Enter name: ");
                        String name = sc.nextLine();
                        System.out.print("Enter email: ");
                        String email = sc.nextLine();
                        System.out.print("Create 4-digit Security PIN: ");
                        String pin = sc.nextLine();
                        System.out.print("Account type (savings/current): ");
                        String type = sc.nextLine();
                        System.out.print("Enter opening balance: ");
                        double bal = sc.nextDouble();

                        double limit = 0;
                        if (type.equalsIgnoreCase("current")) {
                            System.out.print("Enter overdraft limit: ");
                            limit = sc.nextDouble();
                        }
                        sc.nextLine();

                        Account a = bank.openAccount(name, email, pin, type, bal, limit);
                        if (a != null) {
                            System.out.println("Account opened! Acc No is: " + a.accNo);
                        }
                        break;

                    case 2:
                        System.out.print("Enter account number to deposit into: ");
                        String dAcc = sc.nextLine();
                        System.out.print("Enter depositor name/remark: ");
                        String depName = sc.nextLine();
                        System.out.print("Enter amount to deposit: ");
                        double dAmt = sc.nextDouble();
                        sc.nextLine();

                        bank.doDeposit(dAcc, dAmt, depName);
                        System.out.println("Deposit completed!");
                        break;

                    case 3:
                        System.out.print("Enter account number: ");
                        String wAcc = sc.nextLine();
                        System.out.print("Enter Security PIN: ");
                        String wPin = sc.nextLine();
                        System.out.print("Enter amount to withdraw: ");
                        double wAmt = sc.nextDouble();
                        sc.nextLine();

                        bank.doWithdraw(wAcc, wAmt, wPin);
                        break;

                    case 4:
                        System.out.print("Enter sender account: ");
                        String from = sc.nextLine();
                        System.out.print("Enter sender Security PIN: ");
                        String sPin = sc.nextLine();
                        System.out.print("Enter receiver account: ");
                        String to = sc.nextLine();
                        System.out.print("Enter transfer amount: ");
                        double tAmt = sc.nextDouble();
                        sc.nextLine();

                        bank.doTransfer(from, to, tAmt, sPin);
                        break;

                    case 5:
                        System.out.print("Enter account number: ");
                        String cAcc = sc.nextLine();
                        Account acc = bank.findAccount(cAcc);
                        System.out.println("\n--- Account Details ---");
                        acc.display();
                        break;

                    case 6:
                        System.out.print("Enter account number to view passbook: ");
                        String pAcc = sc.nextLine();
                        bank.showPassbook(pAcc);

                        System.out.print("Do you want to see all transactions sorted by amount? (y/n): ");
                        String opt = sc.nextLine();
                        if (opt.equalsIgnoreCase("y")) {
                            bank.showSortedTransactions();
                        }
                        break;

                    case 7:
                        bank.showSummary();
                        break;

                    case 8:
                        System.out.println("Exiting system. Bye!");
                        break;

                    default:
                        System.out.println("Invalid choice! Enter between 1 to 8.");
                }
            } catch (InsufficientBalanceException e) {
                System.out.println("Balance Error: " + e.getMessage());
            } catch (InvalidAmountException e) {
                System.out.println("Amount Error: " + e.getMessage());
            } catch (AccountNotFoundException e) {
                System.out.println("Search Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                sc.nextLine();
            }
        }

        sc.close();
    }
}
