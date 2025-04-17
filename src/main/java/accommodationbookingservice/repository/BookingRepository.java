package accommodationbookingservice.repository;

import accommodationbookingservice.model.Accommodation;
import accommodationbookingservice.model.Booking;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b "
            + "FROM Booking b "
            + "WHERE b.accommodation = :accommodation "
            + "AND (b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate) "
            + "AND (:excludeBookingId IS NULL OR b.id != :excludeBookingId) "
            + "AND b.status NOT IN (:canceledStatus, :expiredStatus)")
    List<Booking> findOverlappingBookings(
            @Param("accommodation") Accommodation accommodation,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("excludeBookingId") Long excludeBookingId,
            @Param("canceledStatus") Booking.BookingStatus canceledStatus,
            @Param("expiredStatus") Booking.BookingStatus expiredStatus);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByStatus(Booking.BookingStatus status, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(
            Long userId, Booking.BookingStatus status, Pageable pageable);

    List<Booking> findAllByCheckOutDateLessThanEqualAndStatusNot(
            LocalDate tomorrow, Booking.BookingStatus bookingStatus);

    Page<Booking> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "accommodation")
    Optional<Booking> findById(Long bookingId);
}
