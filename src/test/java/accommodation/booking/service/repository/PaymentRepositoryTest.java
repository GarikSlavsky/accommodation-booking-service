package accommodation.booking.service.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import accommodation.booking.service.config.CustomPostgresContainer;
import accommodation.booking.service.model.Payment;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {
        "classpath:database/user/add-users.sql",
        "classpath:database/user/add-additional-users.sql",
        "classpath:database/accommodation/add-accommodations.sql",
        "classpath:database/booking/add-bookings.sql",
        "classpath:database/booking/add-additional-bookings.sql",
        "classpath:database/payment/add-payments.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {
        "classpath:database/payment/remove-all-payments.sql",
        "classpath:database/booking/remove-all-bookings.sql",
        "classpath:database/accommodation/remove-all-accommodations.sql",
        "classpath:database/user/remove-all-users.sql"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public class PaymentRepositoryTest {
    @Container
    private static final CustomPostgresContainer postgresContainer =
            CustomPostgresContainer.getInstance();
    @Autowired
    private PaymentRepository paymentRepository;
    private Long validPaymentId;
    private Long validUserId;
    private Payment.PaymentStatus validStatus;

    @BeforeEach
    void setUp() {
        validPaymentId = 23L;
        validUserId = 2L;
        validStatus = Payment.PaymentStatus.PENDING;
    }

    @Test
    @DisplayName("Find payment by ID, user ID, and status when payment exists")
    void findByIdAndUserIdAndStatus_ValidCriteria_ReturnsPayment() {
        assertTrue(postgresContainer.isRunning());
        Optional<Payment> actual = paymentRepository.findByIdAndUserIdAndStatus(
                validPaymentId, validUserId, validStatus);
        assertTrue(actual.isPresent(), "Payment should be found");
        Payment payment = actual.get();
        assertEquals(validPaymentId, payment.getId());
        assertEquals(validStatus, payment.getStatus());
        assertEquals(validUserId, payment.getBooking().getUser().getId());
    }

    @Test
    @DisplayName("Return empty when payment ID does not exist")
    void findByIdAndUserIdAndStatus_NonExistentPaymentId_ReturnsEmpty() {
        Long nonexistentPaymentId = 999L;
        Optional<Payment> actual = paymentRepository.findByIdAndUserIdAndStatus(
                nonexistentPaymentId, validUserId, validStatus);
        assertFalse(actual.isPresent(),
                "No payment should be found for non-existent payment ID");
    }

    @Test
    @DisplayName("Return empty when user ID does not match")
    void findByIdAndUserIdAndStatus_InvalidUserId_ReturnsEmpty() {
        Long nonexistentUserId = 999L;
        Optional<Payment> actual = paymentRepository.findByIdAndUserIdAndStatus(
                validPaymentId, nonexistentUserId, validStatus);
        assertFalse(actual.isPresent(), "No payment should be found for incorrect user ID");
    }

    @Test
    @DisplayName("Return empty when status does not match")
    void findByIdAndUserIdAndStatus_InvalidStatus_ReturnsEmpty() {
        Payment.PaymentStatus unusedStatus = Payment.PaymentStatus.EXPIRED;
        Optional<Payment> actual = paymentRepository.findByIdAndUserIdAndStatus(
                validPaymentId, validUserId, unusedStatus);
        assertFalse(actual.isPresent(), "No payment should be found for incorrect status");
    }
}
