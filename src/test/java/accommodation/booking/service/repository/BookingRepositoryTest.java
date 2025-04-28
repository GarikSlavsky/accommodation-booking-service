package accommodation.booking.service.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import accommodation.booking.service.config.CustomPostgresContainer;
import accommodation.booking.service.model.Booking;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        "classpath:database/booking/add-additional-bookings.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {
        "classpath:database/booking/remove-all-bookings.sql",
        "classpath:database/accommodation/remove-all-accommodations.sql",
        "classpath:database/user/remove-all-users.sql"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public class BookingRepositoryTest {
    @Container
    private static final CustomPostgresContainer postgresContainer =
            CustomPostgresContainer.getInstance();

    @Autowired
    private BookingRepository bookingRepository;
    private Long validUserId;
    private Booking.BookingStatus validStatus;
    private Pageable pageable;
    private Long accommodationId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long excludeBookingId;
    private String canceledStatus;
    private String expiredStatus;

    @BeforeEach
    void setUp() {
        validUserId = 1L;
        validStatus = Booking.BookingStatus.PENDING;
        pageable = PageRequest.of(0, 10);
        accommodationId = 15L;
        checkInDate = LocalDate.of(2025, 5, 1);
        checkOutDate = LocalDate.of(2025, 5, 15);
        excludeBookingId = null; // Default: no booking excluded
        canceledStatus = "CANCELED";
        expiredStatus = "EXPIRED";
    }

    @Test
    @DisplayName("Find bookings by user ID and status when matches exist")
    void findByOptionalUserIdAndStatus_ValidUserIdAndStatus_ReturnsBookings() {
        Page<Booking> result = bookingRepository.findByOptionalUserIdAndStatus(
                validUserId, validStatus, pageable);
        assertNotNull(result, "Result page should not be null");
        assertTrue(result.hasContent(), "Bookings should be found");
        List<Booking> bookings = result.getContent();
        assertEquals(2, bookings.size(), """
                Expected exactly 2 bookings,
                but the bookings list does not meet this criteria.
                Ensure the bookings are correctly populated.
                """);
        assertTrue(bookings.stream()
                        .allMatch(b -> b.getUser()
                                .getId()
                                .equals(validUserId)),
                "All bookings should belong to the specified user");
        assertTrue(bookings.stream()
                        .allMatch(b -> b.getStatus() == validStatus),
                "All bookings should have the specified status");
    }

    @Test
    @DisplayName("Find bookings by user ID only when matches exist")
    void findByOptionalUserIdAndStatus_ValidUserIdNullStatus_ReturnsBookings() {
        Page<Booking> result = bookingRepository.findByOptionalUserIdAndStatus(
                validUserId, null, pageable);
        assertNotNull(result, "Result page should not be null");
        assertTrue(result.hasContent(), "Bookings should be found");
        List<Booking> bookings = result.getContent();
        assertEquals(3, bookings.size(), """
                Expected exactly 3 bookings,
                but the bookings list does not meet this criteria.
                Ensure the bookings are correctly populated.
                """);
        assertTrue(bookings.stream()
                        .allMatch(b -> b.getUser()
                                .getId()
                                .equals(validUserId)),
                "All bookings should belong to the specified user");
    }

    @Test
    @DisplayName("Find bookings by status only when matches exist")
    void findByOptionalUserIdAndStatus_NullUserIdValidStatus_ReturnsBookings() {
        Page<Booking> result = bookingRepository.findByOptionalUserIdAndStatus(
                null, validStatus, pageable);
        assertNotNull(result, "Result page should not be null");
        assertTrue(result.hasContent(), "Bookings should be found");
        List<Booking> bookings = result.getContent();
        assertEquals(6, bookings.size(), """
                Expected exactly 6 bookings,
                but the bookings list does not meet this criteria.
                Ensure the bookings are correctly populated.
                """);
        assertTrue(bookings.stream()
                        .allMatch(b -> b.getStatus() == validStatus),
                "All bookings should have the specified status");
    }

    @Test
    @DisplayName("Find all bookings when both user ID and status are null")
    void findByOptionalUserIdAndStatus_NullUserIdAndStatus_ReturnsAllBookings() {
        Page<Booking> result = bookingRepository.findByOptionalUserIdAndStatus(
                null, null, pageable);
        assertNotNull(result, "Result page should not be null");
        assertTrue(result.hasContent(), "Bookings should be found");
        assertEquals(pageable.getPageSize(), result.getSize(),
                "Page size should match requested size");
    }

    @Test
    @DisplayName("Return empty page when user ID does not exist")
    void findByOptionalUserIdAndStatus_NonExistentUserId_ReturnsEmptyPage() {
        Long nonExistentUserId = 999L;
        Page<Booking> result = bookingRepository.findByOptionalUserIdAndStatus(
                nonExistentUserId, validStatus, pageable);
        assertNotNull(result, "Result page should not be null");
        assertFalse(result.hasContent(),
                "No bookings should be found for non-existent user ID");
    }

    @Test
    @DisplayName("Return empty page when status does not exist")
    void findByOptionalUserIdAndStatus_NonExistentStatus_ReturnsEmptyPage() {
        Booking.BookingStatus nonExistentStatus = Booking.BookingStatus.CONFIRMED;
        Page<Booking> result = bookingRepository.findByOptionalUserIdAndStatus(
                validUserId, nonExistentStatus, pageable);
        assertNotNull(result, "Result page should not be null");
        assertFalse(result.hasContent(),
                "No bookings should be found for non-existent status");
    }

    @Test
    @DisplayName("Verify pagination works correctly")
    void findByOptionalUserIdAndStatus_ValidCriteriaWithPagination_ReturnsCorrectPage() {
        Pageable smallPageable = PageRequest.of(0, 2);
        Page<Booking> result = bookingRepository.findByOptionalUserIdAndStatus(
                null, null, smallPageable);
        assertNotNull(result, "Result page should not be null");
        assertTrue(result.hasContent(), "Bookings should be found");
        assertEquals(2, result.getPageable().getPageSize(), "Page size should be 2");
        assertTrue(result.getContent().size() <= 2,
                "At most 2 bookings should be returned");
    }

    @Test
    @DisplayName("Find max occupancy when multiple bookings overlap")
    void findMaxOccupancy_OverlappingBookings_ReturnsMaxOccupancy() {
        int actual = bookingRepository.findMaxOccupancy(
                accommodationId, checkInDate, checkOutDate, excludeBookingId,
                canceledStatus, expiredStatus);
        assertEquals(4, actual, """
                Expected max occupancy of 4 on May 8 (bookings 36, 58, 59, 67),
                but the result does not match.""");
    }

    @Test
    @DisplayName("Find max occupancy when no bookings exist for accommodation")
    void findMaxOccupancy_NoBookings_ReturnsZero() {
        Long noBookingAccommodationId = 999L;
        int actual = bookingRepository.findMaxOccupancy(
                noBookingAccommodationId, checkInDate, checkOutDate, excludeBookingId,
                canceledStatus, expiredStatus);
        assertEquals(0, actual, """
                Expected max occupancy of 0 for an accommodation with no bookings,
                but the result does not match.""");
    }

    @Test
    @DisplayName("Find max occupancy when excluding a booking")
    void findMaxOccupancy_ExcludeBooking_ReduceOccupancy() {
        excludeBookingId = 36L;
        int actual = bookingRepository.findMaxOccupancy(
                accommodationId, checkInDate, checkOutDate, excludeBookingId,
                canceledStatus, expiredStatus);
        assertEquals(3, actual, """
                Expected max occupancy of 3 when excluding booking 36,
                but the result does not match.""");
    }

    @Test
    @DisplayName("Find max occupancy for single-day date range")
    void findMaxOccupancy_SingleDayRange_ReturnsCorrectOccupancy() {
        checkInDate = LocalDate.of(2025, 5, 8);
        checkOutDate = LocalDate.of(2025, 5, 8);
        int actual = bookingRepository.findMaxOccupancy(
                accommodationId, checkInDate, checkOutDate, excludeBookingId,
                canceledStatus, expiredStatus);
        assertEquals(4, actual, """
                Expected max occupancy of 4 for single-day range on May 8,
                but the result does not match.""");
    }

    @Test
    @DisplayName("Find max occupancy for date range with no active bookings")
    void findMaxOccupancy_DateRangeNoActiveBookings_ReturnsZero() {
        checkInDate = LocalDate.of(2025, 4, 1);
        checkOutDate = LocalDate.of(2025, 4, 15);
        int actual = bookingRepository.findMaxOccupancy(
                accommodationId, checkInDate, checkOutDate, excludeBookingId,
                canceledStatus, expiredStatus);
        assertEquals(0, actual, """
                Expected max occupancy of 0 for a date range with no active bookings,
                but the result does not match.""");
    }
}
