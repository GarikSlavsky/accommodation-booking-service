package accommodationbookingservice.repository;

import accommodationbookingservice.model.Booking;
import accommodationbookingservice.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findAllByBookingUserId(Long userId, Pageable pageable);

    Page<Payment> findAll(Pageable pageable);

    Optional<Payment> findBySessionId(String sessionId);

    Optional<Payment> findByBooking(Booking booking);

    List<Payment> findByBookingUserIdAndStatus(Long userId, Payment.PaymentStatus status);

    List<Payment> findAllByStatus(Payment.PaymentStatus paymentStatus);
}
