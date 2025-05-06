package accommodation.booking.service.service.booking;

import accommodation.booking.service.dto.booking.BookingRequestDto;
import accommodation.booking.service.dto.booking.BookingResponseDto;
import accommodation.booking.service.dto.booking.BookingStatusPatchRequestDto;
import accommodation.booking.service.model.Booking;
import accommodation.booking.service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingResponseDto createBooking(BookingRequestDto requestDto, User currentUser);

    Page<BookingResponseDto> getBookings(
            Long userId, Booking.BookingStatus status, Pageable pageable);

    Page<BookingResponseDto> getBookingsByUser(Long userId, Pageable pageable);

    BookingResponseDto getBookingById(Long id, User currentUser);

    BookingResponseDto updateBookingDetails(
            Long id, BookingRequestDto requestDto, User currentUser);

    BookingResponseDto updateBookingStatus(Long id, BookingStatusPatchRequestDto patchRequestDto);

    void cancelBooking(Long id, User currentUser);
}
