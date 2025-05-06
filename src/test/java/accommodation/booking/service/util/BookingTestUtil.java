package accommodation.booking.service.util;

import accommodation.booking.service.dto.booking.BookingRequestDto;
import accommodation.booking.service.dto.booking.BookingResponseDto;
import accommodation.booking.service.model.Accommodation;
import accommodation.booking.service.model.Booking;
import accommodation.booking.service.model.User;
import java.time.LocalDate;

public final class BookingTestUtil {
    private static final Booking.BookingStatus STATUS_PENDING = Booking.BookingStatus.PENDING;
    private static final Long ACCOMMODATION_ID = 18L;
    private static final Long BOOKING_ID = 1L;
    private static final Long USER_ID = 1L;

    public static BookingRequestDto initializeBookingRequestDto() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setAccommodationId(ACCOMMODATION_ID);
        dto.setCheckInDate(LocalDate.of(2026, 5, 1));
        dto.setCheckOutDate(LocalDate.of(2026, 5, 5));
        return dto;
    }

    public static User initializeUser(Long userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }

    public static Accommodation initializeAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(ACCOMMODATION_ID);
        accommodation.setAvailability(5);
        return accommodation;
    }

    public static Booking initializeBooking(
            BookingRequestDto dto, User user, Accommodation accommodation) {

        Booking booking = new Booking();
        booking.setId(BOOKING_ID);
        booking.setAccommodation(accommodation);
        booking.setUser(user);
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setStatus(STATUS_PENDING);
        return booking;
    }

    public static BookingResponseDto initializeBookingResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setAccommodationId(ACCOMMODATION_ID);
        dto.setUserId(USER_ID);
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setStatus(booking.getStatus());
        return dto;
    }

    public static BookingResponseDto initializeBookingResponseDtoDirectlyFromRequest(
            BookingRequestDto dto) {

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(BOOKING_ID);
        responseDto.setAccommodationId(dto.getAccommodationId());
        responseDto.setCheckInDate(dto.getCheckInDate());
        responseDto.setCheckOutDate(dto.getCheckOutDate());
        return responseDto;
    }
}
