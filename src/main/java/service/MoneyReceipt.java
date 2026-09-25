package service;

import java.math.BigDecimal;

public record MoneyReceipt(String reference, BigDecimal balance) {
}
