public class Transaction {
    int id;
    String accNo;
    String type;
    double amount;
    double remBal;
    String remark;

    public Transaction(int id, String accNo, String type, double amount, double remBal, String remark) {
        this.id = id;
        this.accNo = accNo;
        this.type = type;
        this.amount = amount;
        this.remBal = remBal;
        this.remark = remark;
    }

    public void printTxn() {
        System.out.println("TXN#" + id + " | " + type + " | Amount: Rs " + amount + " | Balance: Rs " + remBal + " | " + remark);
    }
}
