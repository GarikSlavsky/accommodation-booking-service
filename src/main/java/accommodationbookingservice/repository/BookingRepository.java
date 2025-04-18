package accommodationbookingservice.repository;

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
    @Query(value = """
        SELECT COALESCE(MAX(daily_count), 0) as max_occupancy
        FROM (
            SELECT g.date, COUNT(b.id) as daily_count
            FROM generate_series(:checkInDate, :checkOutDate, interval '1 day') g(date)
            LEFT JOIN booking b
                ON b.accommodation_id = :accommodationId
                AND b.check_in_date <= g.date
                AND b.check_out_date >= g.date
                AND (:excludeBookingId IS NULL OR b.id != :excludeBookingId)
                AND b.status NOT IN (:canceledStatus, :expiredStatus)
            GROUP BY g.date
        ) daily_counts""",
            nativeQuery = true)
    int findMaxOccupancy(
            @Param("accommodationId") Long accommodationId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("excludeBookingId") Long excludeBookingId,
            @Param("canceledStatus") String canceledStatus,
            @Param("expiredStatus") String expiredStatus);

    List<Booking> findAllByCheckOutDateLessThanEqualAndStatusNot(
            LocalDate tomorrow, Booking.BookingStatus bookingStatus);

    @Query("SELECT b "
            + "FROM Booking b "
            + "WHERE (:userId IS NULL OR b.user.id = :userId) "
            + "AND (:status IS NULL OR b.status = :status)")
    Page<Booking> findByOptionalUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") Booking.BookingStatus status,
            Pageable pageable);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "accommodation")
    Optional<Booking> findById(Long bookingId);
}
