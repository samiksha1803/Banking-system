package dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import util.TimeUtil;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, length = 30)
    private String username;

    private String password;

    @Column(length = 80)
    private String fullName;

    @Column(unique = true, length = 120)
    private String email;

    @Column(length = 15)
    private String phone;

    @Column(unique = true, length = 16)
    private String accountNumber;

    @Column(precision = 14, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(length = 20)
    private String role = "USER";

    private Boolean active = Boolean.TRUE;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = TimeUtil.now();
        }
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (role == null || role.isBlank()) {
            role = "USER";
        }
        if (active == null) {
            active = Boolean.TRUE;
        }
    }

    public BigDecimal safeBalance() {
        return balance == null ? BigDecimal.ZERO : balance;
    }

    public boolean isEnabled() {
        return active == null || active;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public String displayName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        return username;
    }
}
