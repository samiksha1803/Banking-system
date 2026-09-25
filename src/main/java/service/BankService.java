package service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;

import dao.UserDao;
import dto.Notification;
import dto.Transaction;
import dto.User;
import exception.BankException;
import util.AccountUtil;
import util.JpaUtil;
import util.MailService;
import util.Money;
import util.PasswordUtil;
import util.TimeUtil;
import util.Validators;

/**
 * Banking rules live here so a deposit, withdrawal, or transfer updates the
 * balance, writes the ledger row, and stores the alert in one database transaction.
 */
public class BankService {

    private final MailService mail = new MailService();

    public boolean isMailEnabled() {
        return mail.isEnabled();
    }

    public void prepareDatabase() {
        inTx(em -> {
            List<User> users = em.createQuery("select u from User u", User.class).getResultList();
            Set<String> usedAccounts = new HashSet<>();
            for (User user : users) {
                if (user.getAccountNumber() != null && !user.getAccountNumber().isBlank()) {
                    usedAccounts.add(user.getAccountNumber());
                }
            }
            for (User user : users) {
                if (user.getAccountNumber() == null || user.getAccountNumber().isBlank()) {
                    String number = AccountUtil.accountNumber(usedAccounts);
                    usedAccounts.add(number);
                    user.setAccountNumber(number);
                }
                if (user.getRole() == null || user.getRole().isBlank()) {
                    user.setRole("USER");
                }
                if (user.getActive() == null) {
                    user.setActive(Boolean.TRUE);
                }
                if (user.getBalance() == null) {
                    user.setBalance(BigDecimal.ZERO);
                }
                if (user.getFullName() == null || user.getFullName().isBlank()) {
                    user.setFullName(user.getUsername());
                }
                if (user.getCreatedAt() == null) {
                    user.setCreatedAt(TimeUtil.now());
                }
            }

            List<Transaction> legacy = em.createQuery(
                    "select t from Transaction t where t.referenceNumber is null or t.createdAt is null",
                    Transaction.class).getResultList();
            for (Transaction txn : legacy) {
                if (txn.getCreatedAt() == null) {
                    txn.setCreatedAt(TimeUtil.now());
                }
                if (txn.getReferenceNumber() == null || txn.getReferenceNumber().isBlank()) {
                    txn.setReferenceNumber(String.format("LEG%08d", txn.getId()));
                }
            }

            boolean adminExists = users.stream()
                    .anyMatch(user -> "admin".equalsIgnoreCase(user.getUsername()));
            if (!adminExists) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(PasswordUtil.hash("admin123"));
                admin.setFullName("System Administrator");
                admin.setEmail("admin@inbank.local");
                String number = AccountUtil.accountNumber(usedAccounts);
                admin.setAccountNumber(number);
                admin.setBalance(BigDecimal.ZERO);
                admin.setRole("ADMIN");
                admin.setActive(Boolean.TRUE);
                admin.setCreatedAt(TimeUtil.now());
                em.persist(admin);
                System.out.println("InBank admin created. Username: admin  Password: admin123");
            }
        });
    }

    public User register(String fullName, String username, String email, String phone,
            String password, String confirm) {
        String name = Validators.personName(fullName);
        String handle = Validators.username(username);
        String mailAddress = Validators.email(email);
        String mobile = Validators.phone(phone);
        Validators.password(password, confirm);

        List<Alert> alerts = new ArrayList<>();
        User created = new User();
        inTx(em -> {
            long usernameCount = em.createQuery(
                    "select count(u) from User u where lower(u.username) = :name", Long.class)
                    .setParameter("name", handle.toLowerCase())
                    .getSingleResult();
            if (usernameCount > 0) {
                throw new BankException("That username is already taken.");
            }
            long emailCount = em.createQuery(
                    "select count(u) from User u where lower(u.email) = :email", Long.class)
                    .setParameter("email", mailAddress)
                    .getSingleResult();
            if (emailCount > 0) {
                throw new BankException("An account with this email already exists.");
            }

            created.setUsername(handle);
            created.setPassword(PasswordUtil.hash(password));
            created.setFullName(name);
            created.setEmail(mailAddress);
            created.setPhone(mobile);
            created.setAccountNumber(freshAccount(em));
            created.setBalance(BigDecimal.ZERO);
            created.setRole("USER");
            created.setActive(Boolean.TRUE);
            created.setCreatedAt(TimeUtil.now());
            em.persist(created);
            em.flush();

            String body = "Hello " + name + ",\n\n"
                    + "Your InBank account is ready.\n"
                    + "Account number: " + created.getAccountNumber() + "\n"
                    + "Username: " + handle + "\n\n"
                    + "Sign in to deposit, withdraw, or transfer money.\n\nInBank";
            remember(em, alerts, created, "Welcome to InBank", body);
        });
        deliver(alerts);
        created.setPassword(null);
        return created;
    }

    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new BankException("Enter your username and password.");
        }
        EntityManager em = JpaUtil.getEntityManager();
        User user;
        try {
            List<User> users = em.createQuery(
                    "select u from User u where lower(u.username) = :name", User.class)
                    .setParameter("name", username.trim().toLowerCase())
                    .setMaxResults(1)
                    .getResultList();
            user = users.isEmpty() ? null : users.get(0);
        } finally {
            em.close();
        }
        if (user == null || !PasswordUtil.matches(password, user.getPassword())) {
            throw new BankException("Those sign-in details do not match our records.");
        }
        if (!user.isEnabled()) {
            throw new BankException("This account is deactivated. Please contact the bank.");
        }
        if (PasswordUtil.isLegacy(user.getPassword())) {
            try {
                new UserDao().updatePassword(user.getId(), PasswordUtil.hash(password));
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
        return user;
    }

    public MoneyReceipt deposit(int userId, BigDecimal amount) {
        BigDecimal value = Money.require(amount);
        List<Alert> alerts = new ArrayList<>();
        MoneyReceipt[] receipt = new MoneyReceipt[1];
        inTx(em -> {
            User user = lockedUser(em, userId);
            user.setBalance(user.safeBalance().add(value));
            String reference = AccountUtil.reference();
            saveTxn(em, user, reference, "DEPOSIT", value, "Cash deposit", null, null);
            String body = letter(user,
                    Money.inr(value) + " was deposited to account " + user.getAccountNumber() + ".\n"
                            + "Reference: " + reference + "\n"
                            + "Balance: " + Money.inr(user.safeBalance()));
            remember(em, alerts, user, "Deposit received", body);
            receipt[0] = new MoneyReceipt(reference, user.safeBalance());
        });
        deliver(alerts);
        return receipt[0];
    }

    public MoneyReceipt withdraw(int userId, BigDecimal amount) {
        BigDecimal value = Money.require(amount);
        List<Alert> alerts = new ArrayList<>();
        MoneyReceipt[] receipt = new MoneyReceipt[1];
        inTx(em -> {
            User user = lockedUser(em, userId);
            if (user.safeBalance().compareTo(value) < 0) {
                throw new BankException("Insufficient balance. Available: " + Money.inr(user.safeBalance()));
            }
            user.setBalance(user.safeBalance().subtract(value));
            String reference = AccountUtil.reference();
            saveTxn(em, user, reference, "WITHDRAW", value, "Cash withdrawal", null, null);
            String body = letter(user,
                    Money.inr(value) + " was withdrawn from account " + user.getAccountNumber() + ".\n"
                            + "Reference: " + reference + "\n"
                            + "Balance: " + Money.inr(user.safeBalance()));
            remember(em, alerts, user, "Withdrawal completed", body);
            receipt[0] = new MoneyReceipt(reference, user.safeBalance());
        });
        deliver(alerts);
        return receipt[0];
    }

    public TransferReceipt transfer(int userId, String accountNumber, BigDecimal amount, String note) {
        BigDecimal value = Money.require(amount);
        String account = Validators.account(accountNumber);
        String memo = Validators.note(note);
        if (memo == null) {
            memo = "Transfer";
        }
        final String memoText = memo;
        List<Alert> alerts = new ArrayList<>();
        TransferReceipt[] receipt = new TransferReceipt[1];
        inTx(em -> {
            User senderPeek = em.find(User.class, userId);
            if (senderPeek == null) {
                throw new BankException("Account not found.");
            }
            List<User> matches = em.createQuery(
                    "select u from User u where u.accountNumber = :account", User.class)
                    .setParameter("account", account)
                    .setMaxResults(1)
                    .getResultList();
            if (matches.isEmpty()) {
                throw new BankException("No account found with that number.");
            }
            User receiverPeek = matches.get(0);
            if (receiverPeek.getId() == senderPeek.getId()) {
                throw new BankException("You cannot transfer money to your own account.");
            }
            if (!receiverPeek.isEnabled()) {
                throw new BankException("That account cannot receive transfers right now.");
            }

            // Lock the lower id first so two transfers cannot deadlock each other.
            int low = Math.min(senderPeek.getId(), receiverPeek.getId());
            int high = Math.max(senderPeek.getId(), receiverPeek.getId());
            User first = em.find(User.class, low, LockModeType.PESSIMISTIC_WRITE);
            User second = em.find(User.class, high, LockModeType.PESSIMISTIC_WRITE);
            User sender = first.getId() == userId ? first : second;
            User receiver = sender == first ? second : first;

            if (sender.safeBalance().compareTo(value) < 0) {
                throw new BankException("Insufficient balance. Available: " + Money.inr(sender.safeBalance()));
            }

            String reference = AccountUtil.reference();
            sender.setBalance(sender.safeBalance().subtract(value));
            saveTxn(em, sender, reference, "TRANSFER_OUT", value, memoText,
                    receiver.getAccountNumber(), receiver.displayName());
            receiver.setBalance(receiver.safeBalance().add(value));
            saveTxn(em, receiver, reference, "TRANSFER_IN", value, memoText,
                    sender.getAccountNumber(), sender.displayName());

            remember(em, alerts, sender, "Transfer sent", letter(sender,
                    Money.inr(value) + " was sent to " + receiver.displayName()
                            + " (" + receiver.getAccountNumber() + ").\n"
                            + "Reference: " + reference + "\n"
                            + "Balance: " + Money.inr(sender.safeBalance())));
            remember(em, alerts, receiver, "Transfer received", letter(receiver,
                    Money.inr(value) + " was received from " + sender.displayName()
                            + " (" + sender.getAccountNumber() + ").\n"
                            + "Reference: " + reference + "\n"
                            + "Balance: " + Money.inr(receiver.safeBalance())));
            receipt[0] = new TransferReceipt(reference, sender.safeBalance(),
                    receiver.displayName(), receiver.getAccountNumber());
        });
        deliver(alerts);
        return receipt[0];
    }

    public void updateProfile(int userId, String fullName, String email, String phone) {
        String name = Validators.personName(fullName);
        String mailAddress = Validators.email(email);
        String mobile = Validators.phone(phone);
        List<Alert> alerts = new ArrayList<>();
        inTx(em -> {
            long emailCount = em.createQuery(
                    "select count(u) from User u where lower(u.email) = :email and u.id <> :id", Long.class)
                    .setParameter("email", mailAddress)
                    .setParameter("id", userId)
                    .getSingleResult();
            if (emailCount > 0) {
                throw new BankException("That email is already used by another account.");
            }
            User user = lockedUser(em, userId);
            user.setFullName(name);
            user.setEmail(mailAddress);
            user.setPhone(mobile);
            remember(em, alerts, user, "Profile updated", letter(user,
                    "Your InBank profile details were updated.\n"
                            + "If you did not make this change, reset your password and contact the bank."));
        });
        deliver(alerts);
    }

    public void changePassword(int userId, String current, String next, String confirm) {
        if (current == null || current.isEmpty()) {
            throw new BankException("Enter your current password.");
        }
        Validators.password(next, confirm);
        if (current.equals(next)) {
            throw new BankException("Choose a password that is different from the current one.");
        }
        List<Alert> alerts = new ArrayList<>();
        inTx(em -> {
            User user = lockedUser(em, userId);
            if (!PasswordUtil.matches(current, user.getPassword())) {
                throw new BankException("Current password is incorrect.");
            }
            user.setPassword(PasswordUtil.hash(next));
            remember(em, alerts, user, "Password changed", letter(user,
                    "Your InBank password was changed.\n"
                            + "If you did not do this, contact the bank immediately."));
        });
        deliver(alerts);
    }

    public void setActive(int actorId, int userId, boolean active) {
        if (actorId == userId) {
            throw new BankException("You cannot change your own access.");
        }
        List<Alert> alerts = new ArrayList<>();
        inTx(em -> {
            User user = em.find(User.class, userId);
            if (user == null) {
                throw new BankException("User not found.");
            }
            if (user.isAdmin()) {
                throw new BankException("Administrator access cannot be changed here.");
            }
            user.setActive(active);
            String subject = active ? "Account reactivated" : "Account deactivated";
            String body = active
                    ? "Your InBank account is active again. You can sign in."
                    : "Your InBank account has been deactivated. Contact the bank if this is unexpected.";
            remember(em, alerts, user, subject, letter(user, body));
        });
        deliver(alerts);
    }

    public MoneyReceipt adjust(int userId, String direction, BigDecimal amount, String reason) {
        BigDecimal value = Money.require(amount);
        String memo = Validators.note(reason);
        if (memo == null || memo.length() < 3) {
            throw new BankException("Enter a short reason for this adjustment.");
        }
        boolean credit = "CREDIT".equalsIgnoreCase(direction);
        boolean debit = "DEBIT".equalsIgnoreCase(direction);
        if (!credit && !debit) {
            throw new BankException("Choose credit or debit.");
        }
        List<Alert> alerts = new ArrayList<>();
        MoneyReceipt[] receipt = new MoneyReceipt[1];
        inTx(em -> {
            User user = em.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
            if (user == null) {
                throw new BankException("Account not found.");
            }
            if (debit && user.safeBalance().compareTo(value) < 0) {
                throw new BankException("Insufficient balance. Available: " + Money.inr(user.safeBalance()));
            }
            user.setBalance(credit
                    ? user.safeBalance().add(value)
                    : user.safeBalance().subtract(value));
            String reference = AccountUtil.reference();
            String type = credit ? "ADJUSTMENT_CREDIT" : "ADJUSTMENT_DEBIT";
            saveTxn(em, user, reference, type, value, "Adjustment: " + memo, null, null);
            String body = letter(user,
                    (credit ? "A credit of " : "A debit of ") + Money.inr(value)
                            + " was applied to account " + user.getAccountNumber() + ".\n"
                            + "Reason: " + memo + "\n"
                            + "Reference: " + reference + "\n"
                            + "Balance: " + Money.inr(user.safeBalance()));
            remember(em, alerts, user, credit ? "Account credited" : "Account debited", body);
            receipt[0] = new MoneyReceipt(reference, user.safeBalance());
        });
        deliver(alerts);
        return receipt[0];
    }

    private User lockedUser(EntityManager em, int userId) {
        User user = em.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
        if (user == null) {
            throw new BankException("Account not found.");
        }
        if (!user.isEnabled()) {
            throw new BankException("This account is deactivated.");
        }
        return user;
    }

    private String freshAccount(EntityManager em) {
        for (int attempt = 0; attempt < 20; attempt++) {
            String number = AccountUtil.accountNumber(null);
            Long count = em.createQuery(
                    "select count(u) from User u where u.accountNumber = :number", Long.class)
                    .setParameter("number", number)
                    .getSingleResult();
            if (count == 0) {
                return number;
            }
        }
        throw new BankException("Could not generate an account number. Please try again.");
    }

    private void saveTxn(EntityManager em, User user, String reference, String type,
            BigDecimal amount, String note, String counterpartyAccount, String counterpartyName) {
        Transaction txn = new Transaction();
        txn.setUser(user);
        txn.setReferenceNumber(reference);
        txn.setType(type);
        txn.setAmount(amount);
        txn.setBalanceAfter(user.safeBalance());
        txn.setNote(note);
        txn.setCounterpartyAccount(counterpartyAccount);
        txn.setCounterpartyName(counterpartyName);
        txn.setCreatedAt(TimeUtil.now());
        em.persist(txn);
    }

    private void remember(EntityManager em, List<Alert> alerts, User user, String subject, String body) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setSubject(subject);
        notification.setMessage(trimMessage(body));
        notification.setOpened(false);
        notification.setCreatedAt(TimeUtil.now());
        em.persist(notification);
        alerts.add(new Alert(user.getEmail(), subject, body));
    }

    private String letter(User user, String detail) {
        return "Hello " + user.displayName() + ",\n\n" + detail + "\n\nInBank";
    }

    private String trimMessage(String body) {
        String compact = body.replace("\n\nInBank", "").replaceAll("\\s+", " ").trim();
        if (compact.length() <= 500) {
            return compact;
        }
        return compact.substring(0, 497) + "...";
    }

    private void deliver(List<Alert> alerts) {
        for (Alert alert : alerts) {
            mail.send(alert.email, alert.subject, alert.body);
        }
    }

    private void inTx(Consumer<EntityManager> work) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            work.accept(em);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            if (ex instanceof BankException) {
                throw ex;
            }
            Throwable cause = ex;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            String message = cause.getMessage() == null ? "" : cause.getMessage().toLowerCase();
            if (message.contains("unique") || message.contains("duplicate")) {
                throw new BankException("An account with those details already exists.");
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    private record Alert(String email, String subject, String body) {
    }
}
