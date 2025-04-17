package accommodationbookingservice.repository;

import java.util.List;
import java.util.Optional;
import accommodationbookingservice.model.Booking;
import accommodationbookingservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByBookingUserId(Long userId);

    Optional<Payment> findBySessionId(String sessionId);

    Optional<Payment> findByBooking(Booking booking);

    List<Payment> findByBookingUserIdAndStatus(Long userId, Payment.PaymentStatus status);

    List<Payment> findAllByStatus(Payment.PaymentStatus paymentStatus);
}
