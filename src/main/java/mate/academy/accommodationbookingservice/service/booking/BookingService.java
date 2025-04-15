package mate.academy.accommodationbookingservice.service.booking;

import java.util.List;
import mate.academy.accommodationbookingservice.dto.booking.BookingRequestDto;
import mate.academy.accommodationbookingservice.dto.booking.BookingResponseDto;
import mate.academy.accommodationbookingservice.dto.booking.BookingStatusPatchRequestDto;
import mate.academy.accommodationbookingservice.model.User;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingResponseDto createBooking(BookingRequestDto requestDto, User currentUser);

    List<BookingResponseDto> getBookings(Long userId, String status, Pageable pageable);

    List<BookingResponseDto> getBookingsByUser(Long userId, Pageable pageable);

    BookingResponseDto getBookingById(Long id, User currentUser);

    BookingResponseDto updateBookingDetails(
            Long id, BookingRequestDto requestDto, User currentUser);

    BookingResponseDto updateBookingStatus(Long id, BookingStatusPatchRequestDto patchRequestDto);

    void cancelBooking(Long id, User currentUser);
}
