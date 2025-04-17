package accommodationbookingservice.service.booking;

import accommodationbookingservice.dto.booking.BookingRequestDto;
import accommodationbookingservice.dto.booking.BookingResponseDto;
import accommodationbookingservice.dto.booking.BookingStatusPatchRequestDto;
import accommodationbookingservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingResponseDto createBooking(BookingRequestDto requestDto, User currentUser);

    Page<BookingResponseDto> getBookings(Long userId, String status, Pageable pageable);

    Page<BookingResponseDto> getBookingsByUser(Long userId, Pageable pageable);

    BookingResponseDto getBookingById(Long id, User currentUser);

    BookingResponseDto updateBookingDetails(
            Long id, BookingRequestDto requestDto, User currentUser);

    BookingResponseDto updateBookingStatus(Long id, BookingStatusPatchRequestDto patchRequestDto);

    void cancelBooking(Long id, User currentUser);
}
