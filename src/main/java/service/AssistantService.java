package service;

import java.util.List;
import java.util.Locale;

import dao.TransactionDao;
import dto.Transaction;
import dto.User;
import util.Money;
import util.TimeUtil;
import util.Txn;

/**
 * Answers account questions from the signed-in customer's own records.
 * It is a built-in assistant, not a call to an external AI service, so the demo
 * works offline and never sends banking data to a third party.
 */
public class AssistantService {

    private final TransactionDao transactions = new TransactionDao();

    public String reply(User user, String message) {
        String text = message == null ? "" : message.trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) {
            return "Ask me about your balance, recent transactions, transfers, or password.";
        }
        if (contains(text, "help", "what can you", "how can you")) {
            return "I can help with your InBank account, " + firstName(user) + ". Try asking:\n"
                    + "• What is my balance?\n"
                    + "• What is my account number?\n"
                    + "• Show my recent transactions\n"
                    + "• How do I transfer money?\n"
                    + "• How do I change my password?";
        }
        if (contains(text, "password")) {
            return "Open Change Password from the menu. Enter your current password, then a new one of at least 6 characters. "
                    + "You will get an alert after it changes.";
        }
        if (contains(text, "who am i", "my details", "my profile")) {
            return "You are " + user.displayName()
                    + " (@" + user.getUsername() + ").\n"
                    + "Account number: " + user.getAccountNumber() + "\n"
                    + "Email: " + blank(user.getEmail()) + "\n"
                    + "Phone: " + blank(user.getPhone()) + "\n"
                    + "Balance: " + Money.inr(user.safeBalance());
        }
        if (contains(text, "account number", "account no")) {
            return "Your account number is " + user.getAccountNumber()
                    + ". Share that number with someone who wants to transfer money to you.";
        }
        if (contains(text, "balance", "how much", "available fund")) {
            return "Your available balance is " + Money.inr(user.safeBalance()) + ".";
        }
        if (contains(text, "transaction", "statement", "history", "recent", "last payment")) {
            return history(user, text.contains("today"));
        }
        if (contains(text, "transfer", "send money")) {
            return "Open Transfer, enter the other person's 12-digit account number, the amount, and an optional note. "
                    + "InBank rejects the transfer if your balance is too low, or if the account is yours or inactive. "
                    + "Both of you get the same reference number.";
        }
        if (contains(text, "withdraw")) {
            return "Use Withdraw on the dashboard. The amount has to be greater than zero and no higher than your balance of "
                    + Money.inr(user.safeBalance()) + ". Each withdrawal is limited to " + Money.inr(new java.math.BigDecimal("1000000")) + ".";
        }
        if (contains(text, "deposit", "add money")) {
            return "Use Deposit on the dashboard. The money is added immediately and a reference number is stored in your transaction history.";
        }
        if (contains(text, "profile", "email", "phone", "mobile")) {
            return "Open Profile to update your name, email, and mobile number. Your username and account number stay the same.";
        }
        if (contains(text, "hello", "hi", "hey")) {
            return "Hello " + firstName(user) + ". Ask me for your balance, account number, or recent transactions.";
        }
        return "I can look up your balance, account number, and recent transactions, and I can explain deposits, withdrawals, transfers, and password changes. "
                + "Try: \"What is my balance?\"";
    }

    private String history(User user, boolean todayOnly) {
        List<Transaction> rows = todayOnly
                ? transactions.search(user.getId(), null, TimeUtil.now().toLocalDate(), TimeUtil.now().toLocalDate(), null)
                : transactions.recent(user.getId(), 8);
        if (rows.size() > 8) {
            rows = rows.subList(0, 8);
        }
        if (rows.isEmpty()) {
            return todayOnly
                    ? "You have no transactions yet today."
                    : "You do not have any transactions yet. A deposit is the quickest way to start.";
        }
        StringBuilder reply = new StringBuilder(todayOnly
                ? "Here are today's transactions I can see:\n"
                : "Here are your latest transactions:\n");
        int index = 1;
        for (Transaction txn : rows) {
            reply.append(index++)
                    .append(". ")
                    .append(TimeUtil.format(txn.getCreatedAt()))
                    .append(" · ")
                    .append(Txn.label(txn.getType()))
                    .append(" · ")
                    .append(txn.isCredit() ? "+" : "-")
                    .append(Money.inr(txn.getAmount()))
                    .append(" · ")
                    .append(txn.getReferenceNumber() == null ? "no reference" : txn.getReferenceNumber())
                    .append("\n");
        }
        return reply.toString().trim();
    }

    private boolean contains(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private String firstName(User user) {
        String name = user.displayName();
        int space = name.indexOf(' ');
        return space < 0 ? name : name.substring(0, space);
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? "not added" : value;
    }
}
