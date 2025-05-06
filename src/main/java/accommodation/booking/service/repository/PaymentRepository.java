package accommodation.booking.service.repository;

import accommodation.booking.service.model.Booking;
import accommodation.booking.service.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findAllByBookingUserId(Long userId, Pageable pageable);

    Page<Payment> findAll(Pageable pageable);

    Optional<Payment> findBySessionId(String sessionId);

    Optional<Payment> findByBooking(Booking booking);

    List<Payment> findByBookingUserIdAndStatus(Long userId, Payment.PaymentStatus status);

    List<Payment> findAllByStatus(Payment.PaymentStatus paymentStatus);

    @Query("SELECT p "
            + "FROM Payment p "
            + "WHERE p.id = :paymentId AND p.booking.user.id = :userId AND p.status = :status")
    Optional<Payment> findByIdAndUserIdAndStatus(
            @Param("paymentId") Long paymentId,
            @Param("userId") Long userId,
            @Param("status") Payment.PaymentStatus status);
}
