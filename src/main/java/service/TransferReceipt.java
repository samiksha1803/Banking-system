package service;

import java.math.BigDecimal;

public record TransferReceipt(
        String reference,
        BigDecimal balance,
        String recipientName,
        String recipientAccount) {
}
