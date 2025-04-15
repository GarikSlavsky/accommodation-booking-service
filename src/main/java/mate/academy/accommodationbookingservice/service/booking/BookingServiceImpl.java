package mate.academy.accommodationbookingservice.service.booking;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mate.academy.accommodationbookingservice.dto.booking.BookingRequestDto;
import mate.academy.accommodationbookingservice.dto.booking.BookingResponseDto;
import mate.academy.accommodationbookingservice.dto.booking.BookingStatusPatchRequestDto;
import mate.academy.accommodationbookingservice.exceptions.AccommodationNotAvailableException;
import mate.academy.accommodationbookingservice.mapper.BookingMapper;
import mate.academy.accommodationbookingservice.model.Accommodation;
import mate.academy.accommodationbookingservice.model.Booking;
import mate.academy.accommodationbookingservice.model.Payment;
import mate.academy.accommodationbookingservice.model.User;
import mate.academy.accommodationbookingservice.repository.AccommodationRepository;
import mate.academy.accommodationbookingservice.repository.BookingRepository;
import mate.academy.accommodationbookingservice.repository.PaymentRepository;
import mate.academy.accommodationbookingservice.service.notification.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final PaymentRepository paymentRepository;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;

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

        notificationService.sendNotification(
                String.format("New booking created: ID=%d, Accommodation=%s, Dates=%s to %s",
                        savedBooking.getId(), accommodation.getId(),
                        requestDto.getCheckInDate(), requestDto.getCheckOutDate())
        );
        return bookingMapper.intoDto(savedBooking);
    }

    @Override
    public List<BookingResponseDto> getBookings(Long userId, String status, Pageable pageable) {
        Page<Booking> bookings;
        if (userId != null && status != null) {
            Booking.BookingStatus bookingStatus = matchStatusByRequestParameter(status);
            bookings = bookingRepository.findByUserIdAndStatus(userId, bookingStatus, pageable);
        } else if (userId != null) {
            bookings = bookingRepository.findByUserId(userId, pageable);
        } else if (status != null) {
            Booking.BookingStatus bookingStatus = matchStatusByRequestParameter(status);
            bookings = bookingRepository.findByStatus(bookingStatus, pageable);
        } else {
            bookings = bookingRepository.findAll(pageable);
        }

        return bookings.stream()
                .map(bookingMapper::intoDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByUser(Long userId, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByUserId(userId, pageable);
        return bookings.stream()
                .map(bookingMapper::intoDto)
                .toList();
    }

    @Override
    public BookingResponseDto getBookingById(Long id, User currentUser) {
        Booking booking = retrieveBookingById(id);
        if (!currentUser.getRole().equals(User.UserRole.ROLE_MANAGER)
                && !booking.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Access denied: You are not authorized to view this booking.");
        }
        return bookingMapper.intoDto(booking);
    }

    @Override
    public BookingResponseDto updateBookingDetails(
            Long id, BookingRequestDto requestDto, User currentUser) {

        Booking booking = retrieveBookingById(id);
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Access denied: You can only update your own bookings.");
        }

        Accommodation accommodation = retrieveAccommodationById(requestDto.getAccommodationId());
        LocalDate checkInDate = requestDto.getCheckInDate();
        LocalDate checkOutDate = requestDto.getCheckOutDate();
        validateAccommodationAvailability(
                accommodation, checkInDate, checkOutDate, booking.getId());

        booking.setAccommodation(accommodation);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.intoDto(updatedBooking);
    }

    @Override
    public BookingResponseDto updateBookingStatus(
            Long id, BookingStatusPatchRequestDto patchRequestDto) {
        Booking booking = retrieveBookingById(id);
        Booking.BookingStatus newStatus =
                Booking.BookingStatus.valueOf(patchRequestDto.getStatus().toUpperCase());
        preventDuplicateCancellations(booking, newStatus);
        booking.setStatus(newStatus);
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.intoDto(updatedBooking);
    }

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
        notificationService.sendNotification(
                String.format("Booking canceled: ID=%d, Accommodation=%s, Dates=%s to %s",
                        booking.getId(), accommodation.getId(),
                        booking.getCheckInDate(), booking.getCheckOutDate())
        );
        notificationService.sendNotification(
                String.format("Accommodation released: ID=%d, Type=%s, Location=%s",
                        accommodation.getId(), accommodation.getType(), accommodation.getLocation())
        );
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

    private Booking.BookingStatus matchStatusByRequestParameter(String status) {
        try {
            return Booking.BookingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid booking status: " + status);
        }
    }

    private void validateAccommodationAvailability(Accommodation accommodation,
                                          LocalDate checkInDate,
                                          LocalDate checkOutDate,
                                          Long excludeBookingId) {
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                accommodation, checkInDate, checkOutDate, excludeBookingId,
                Booking.BookingStatus.CANCELED, Booking.BookingStatus.EXPIRED
        );
        int maxOccupancy = calculateMaxOccupancy(overlappingBookings, checkInDate, checkOutDate);
        if (maxOccupancy >= accommodation.getAvailability()) {
            throw new AccommodationNotAvailableException(
                    "Accommodation ID " + accommodation.getId()
                            + " is not available from " + checkInDate
                            + " to " + checkOutDate + ".");
        }
    }

    private int calculateMaxOccupancy(List<Booking> bookings, LocalDate start, LocalDate end) {
        Map<LocalDate, Integer> occupancyByDate = new HashMap<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            int count = 0;
            for (Booking booking : bookings) {
                if (!booking.getCheckInDate().isAfter(current)
                        && !booking.getCheckOutDate().isBefore(current)) {
                    count++;
                }
            }
            occupancyByDate.put(current, count);
            current = current.plusDays(1);
        }
        return occupancyByDate.values()
                .stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
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
