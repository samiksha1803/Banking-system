package dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import util.TimeUtil;

@Entity
@Getter
@Setter
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 32)
    private String referenceNumber;

    @Column(length = 30)
    private String type;

    @Column(precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance", precision = 14, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 160)
    private String note;

    @Column(length = 16)
    private String counterpartyAccount;

    @Column(length = 80)
    private String counterpartyName;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = TimeUtil.now();
        }
    }

    public boolean isCredit() {
        return "DEPOSIT".equals(type)
                || "TRANSFER_IN".equals(type)
                || "ADJUSTMENT_CREDIT".equals(type);
    }

    public String getDetails() {
        String who = "";
        if (counterpartyName != null && !counterpartyName.isBlank()) {
            who = counterpartyName;
        }
        if (counterpartyAccount != null && !counterpartyAccount.isBlank()) {
            who = who.isBlank() ? counterpartyAccount : who + " (" + counterpartyAccount + ")";
        }
        if (note == null || note.isBlank()) {
            return who;
        }
        if (who.isBlank()) {
            return note;
        }
        return note + " · " + who;
    }
}
