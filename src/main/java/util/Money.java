package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import exception.BankException;

public final class Money {

    private static final BigDecimal LIMIT = new BigDecimal("1000000.00");

    private Money() {
    }

    public static String inr(BigDecimal amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));
        return format.format(amount == null ? BigDecimal.ZERO : amount);
    }

    public static BigDecimal parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BankException("Enter an amount.");
        }
        try {
            return require(new BigDecimal(raw.trim()));
        } catch (NumberFormatException ex) {
            throw new BankException("Enter a valid amount.");
        }
    }

    public static BigDecimal require(BigDecimal amount) {
        if (amount == null) {
            throw new BankException("Enter an amount.");
        }
        if (amount.scale() > 2) {
            throw new BankException("Use at most two decimal places.");
        }
        BigDecimal value = amount.setScale(2, RoundingMode.UNNECESSARY);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Amount must be greater than zero.");
        }
        if (value.compareTo(LIMIT) > 0) {
            throw new BankException("Amount exceeds the limit of " + inr(LIMIT) + " per transaction.");
        }
        return value;
    }
}
