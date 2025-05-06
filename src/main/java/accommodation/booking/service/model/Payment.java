package accommodation.booking.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "payments")
@SQLDelete(sql = "UPDATE payments SET is_deleted = true WHERE id = ?")
@SQLRestriction(value = "is_deleted = false")
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, length = 500)
    private String sessionUrl;

    @Column(unique = true, nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(nullable = false)
    private BigDecimal amountToPay;

    public enum PaymentStatus {
        PENDING,
        PAID,
        EXPIRED
    }
}
