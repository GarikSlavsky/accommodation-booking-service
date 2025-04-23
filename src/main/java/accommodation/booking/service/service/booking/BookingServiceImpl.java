package accommodation.booking.service.service.booking;

import static accommodation.booking.service.model.Role.RoleName.ROLE_MANAGER;

import accommodation.booking.service.dto.booking.BookingRequestDto;
import accommodation.booking.service.dto.booking.BookingResponseDto;
import accommodation.booking.service.dto.booking.BookingStatusPatchRequestDto;
import accommodation.booking.service.exceptions.AccommodationNotAvailableException;
import accommodation.booking.service.mapper.BookingMapper;
import accommodation.booking.service.model.Accommodation;
import accommodation.booking.service.model.Booking;
import accommodation.booking.service.model.Payment;
import accommodation.booking.service.model.User;
import accommodation.booking.service.repository.AccommodationRepository;
import accommodation.booking.service.repository.BookingRepository;
import accommodation.booking.service.repository.PaymentRepository;
import accommodation.booking.service.service.notification.BookingNotificationUtil;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final PaymentRepository paymentRepository;
    private final BookingMapper bookingMapper;
    private final BookingNotificationUtil bookingNotificationUtil;

    @Transactional
    @Override
    public BookingResponseDto createBooking(BookingRequestDto requestDto, User currentUser) {
        checkForPendingPayments(currentUser);
        Accommodation accommodation = retrieveAccommodationById(requestDto.getAccommodationId());
        validateAccommodationAvailability(
                accommodation,
                requestDto.getCheckInDate(),
                requestDto.getCheckOutDate(),
                null
        );

        Booking booking = bookingMapper.intoModel(requestDto);
        booking.setUser(currentUser);
        booking.setStatus(Booking.BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        BookingResponseDto responseDto = bookingMapper.intoDto(savedBooking);
        bookingNotificationUtil.notifyBookingCreated(responseDto);
        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> getBookings(
            Long userId, Booking.BookingStatus status, Pageable pageable) {

        Page<Booking> bookings =
                bookingRepository.findByOptionalUserIdAndStatus(userId, status, pageable);
        return bookings.map(bookingMapper::intoDto);
    }

    @Override
    public Page<BookingResponseDto> getBookingsByUser(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable)
                .map(bookingMapper::intoDto);
    }

    @Override
    public BookingResponseDto getBookingById(Long id, User currentUser) {
        Booking booking = retrieveBookingById(id);
        if (!currentUser.getRoles().contains(ROLE_MANAGER)
                && !booking.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Access denied: You are not authorized to view this booking.");
        }
        return bookingMapper.intoDto(booking);
    }

    @Transactional
    @Override
    public BookingResponseDto updateBookingDetails(
            Long id, BookingRequestDto requestDto, User currentUser) {

        Booking booking = retrieveBookingById(id);
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Access denied: You can only update your own bookings.");
        }

        validateAccommodationAvailability(
                booking.getAccommodation(),
                requestDto.getCheckInDate(),
                requestDto.getCheckOutDate(),
                id
        );
        bookingMapper.updateModelFromDto(booking, requestDto);
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.intoDto(updatedBooking);
    }

    @Transactional
    @Override
    public BookingResponseDto updateBookingStatus(
            Long id, BookingStatusPatchRequestDto patchRequestDto) {

        Booking booking = retrieveBookingById(id);
        Booking.BookingStatus newStatus = patchRequestDto.getStatus();
        preventDuplicateCancellations(booking, newStatus);
        booking.setStatus(newStatus);
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.intoDto(updatedBooking);
    }

    @Transactional
    @Override
    public void cancelBooking(Long id, User currentUser) {
        Booking booking = retrieveBookingById(id);
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Access denied: You can only cancel your own bookings.");
        }
        preventDuplicateCancellations(booking, Booking.BookingStatus.CANCELED);
        booking.setStatus(Booking.BookingStatus.CANCELED);
        bookingRepository.save(booking);
        Accommodation accommodation = booking.getAccommodation();
        bookingNotificationUtil.notifyBookingCancelled(booking, accommodation);
        bookingNotificationUtil.notifyAccommodationReleased(accommodation);
    }

    private void checkForPendingPayments(User currentUser) {
        List<Payment> pendingPayments = paymentRepository.findByBookingUserIdAndStatus(
                currentUser.getId(), Payment.PaymentStatus.PENDING
        );
        if (!pendingPayments.isEmpty()) {
            throw new IllegalStateException("Cannot create new booking: You have "
                    + pendingPayments.size()
                    + " pending payment(s). Please complete or renew them first.");
        }
    }

    private void validateAccommodationAvailability(Accommodation accommodation,
                                                   LocalDate checkInDate,
                                                   LocalDate checkOutDate,
                                                   Long excludeBookingId) {
        int maxOccupancy = bookingRepository.findMaxOccupancy(
                accommodation.getId(),
                checkInDate,
                checkOutDate,
                excludeBookingId,
                Booking.BookingStatus.CANCELED.name(),
                Booking.BookingStatus.EXPIRED.name()
        );
        if (maxOccupancy >= accommodation.getAvailability()) {
            throw new AccommodationNotAvailableException(
                    "Accommodation ID " + accommodation.getId()
                            + " is not available from " + checkInDate
                            + " to " + checkOutDate + ".");
        }
    }

    private void preventDuplicateCancellations(
            Booking booking, Booking.BookingStatus targetStatus) {

        if (targetStatus == Booking.BookingStatus.CANCELED
                && booking.getStatus() == Booking.BookingStatus.CANCELED) {
            throw new RuntimeException("This booking has already been canceled.");
        }
    }

    private Booking retrieveBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Booking not found with ID: " + bookingId));
    }

    private Accommodation retrieveAccommodationById(Long accommodationId) {
        return accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Accommodation not found with ID: " + accommodationId));
    }
}
