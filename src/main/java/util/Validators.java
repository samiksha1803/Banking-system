package util;

import java.util.regex.Pattern;

import exception.BankException;

public final class Validators {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^[6-9][0-9]{9}$");
    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z .'-]{1,59}$");
    private static final Pattern ACCOUNT = Pattern.compile("^\\d{12}$");

    private Validators() {
    }

    public static String username(String raw) {
        String value = trim(raw);
        if (!USERNAME.matcher(value).matches()) {
            throw new BankException("Username must be 3 to 20 letters, numbers, or underscores.");
        }
        if ("admin".equalsIgnoreCase(value)) {
            throw new BankException("That username is reserved.");
        }
        return value;
    }

    public static String email(String raw) {
        String value = trim(raw).toLowerCase();
        if (!EMAIL.matcher(value).matches() || value.length() > 120) {
            throw new BankException("Enter a valid email address.");
        }
        return value;
    }

    public static String phone(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String digits = raw.replaceAll("\\s+", "");
        if (!PHONE.matcher(digits).matches()) {
            throw new BankException("Enter a 10-digit Indian mobile number, or leave phone blank.");
        }
        return digits;
    }

    public static String personName(String raw) {
        String value = trim(raw).replaceAll("\\s+", " ");
        if (!NAME.matcher(value).matches()) {
            throw new BankException("Enter a full name using letters, spaces, dots, or hyphens.");
        }
        return value;
    }

    public static String account(String raw) {
        if (raw == null) {
            throw new BankException("Enter the recipient account number.");
        }
        String digits = raw.replaceAll("\\s+", "");
        if (!ACCOUNT.matcher(digits).matches()) {
            throw new BankException("Account numbers are 12 digits.");
        }
        return digits;
    }

    public static void password(String password, String confirm) {
        if (password == null || password.length() < 6 || password.length() > 64) {
            throw new BankException("Password must be 6 to 64 characters.");
        }
        if (!password.equals(confirm)) {
            throw new BankException("Password and confirmation do not match.");
        }
    }

    public static String note(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.trim().replaceAll("\\s+", " ");
        if (value.length() > 120) {
            throw new BankException("Keep the note within 120 characters.");
        }
        return value;
    }

    private static String trim(String raw) {
        return raw == null ? "" : raw.trim();
    }
}
