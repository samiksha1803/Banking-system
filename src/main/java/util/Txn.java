package util;

public final class Txn {

    private Txn() {
    }

    public static String label(String type) {
        if (type == null) {
            return "—";
        }
        return switch (type) {
            case "DEPOSIT" -> "Deposit";
            case "WITHDRAW" -> "Withdrawal";
            case "TRANSFER_IN" -> "Transfer received";
            case "TRANSFER_OUT" -> "Transfer sent";
            case "ADJUSTMENT_CREDIT" -> "Credit adjustment";
            case "ADJUSTMENT_DEBIT" -> "Debit adjustment";
            default -> type;
        };
    }
}
