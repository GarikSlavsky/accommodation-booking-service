package accommodation.booking.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import accommodation.booking.service.dto.booking.BookingRequestDto;
import accommodation.booking.service.dto.booking.BookingResponseDto;
import accommodation.booking.service.exceptions.AccommodationNotAvailableException;
import accommodation.booking.service.mapper.BookingMapper;
import accommodation.booking.service.model.Accommodation;
import accommodation.booking.service.model.Booking;
import accommodation.booking.service.model.Payment;
import accommodation.booking.service.model.User;
import accommodation.booking.service.repository.AccommodationRepository;
import accommodation.booking.service.repository.BookingRepository;
import accommodation.booking.service.repository.PaymentRepository;
import accommodation.booking.service.service.booking.BookingServiceImpl;
import accommodation.booking.service.service.notification.AccommodationNotificationUtil;
import accommodation.booking.service.service.notification.BookingNotificationUtil;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    private static final Booking.BookingStatus STATUS_PENDING = Booking.BookingStatus.PENDING;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private BookingNotificationUtil bookingNotificationUtil;

    @Mock
    private AccommodationNotificationUtil accommodationNotificationUtil;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingRequestDto requestDto;
    private User currentUser;
    private User differentUser;
    private Accommodation accommodation;
    private Booking booking;
    private BookingResponseDto responseDto;
    private static final Long ACCOMMODATION_ID = 1L;
    private static final Long BOOKING_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long DIFFERENT_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        requestDto = initializeBookingRequestDto();
        currentUser = initializeUser(USER_ID);
        differentUser = initializeUser(DIFFERENT_USER_ID);
        accommodation = initializeAccommodation();
        booking = initializeBooking(requestDto);
        responseDto = initializeBookingResponseDto(booking);
    }

    @Test
    @DisplayName("Create booking with valid request and no pending payments.")
    void createBooking_ValidRequestAndNoPendingPayments_ReturnsBookingResponseDto() {
        // Given: Valid request, user with no pending payments, and available accommodation
        when(paymentRepository.findByBookingUserIdAndStatus(USER_ID, Payment.PaymentStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(accommodationRepository.findById(ACCOMMODATION_ID))
                .thenReturn(Optional.of(accommodation));
        when(bookingRepository.findMaxOccupancy(
                ACCOMMODATION_ID,
                requestDto.getCheckInDate(),
                requestDto.getCheckOutDate(),
                null,
                Booking.BookingStatus.CANCELED.name(),
                Booking.BookingStatus.EXPIRED.name()
        )).thenReturn(0);
        when(bookingMapper.intoModel(requestDto)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.intoDto(booking)).thenReturn(responseDto);

        // When: Call the service method
        BookingResponseDto actual = bookingService.createBooking(requestDto, currentUser);

        // Then: Verify the result and interactions
        assertThat(actual).isEqualTo(responseDto);
        assertThat(actual.getStatus()).isEqualTo(STATUS_PENDING);
        verify(paymentRepository).findByBookingUserIdAndStatus(
                USER_ID, Payment.PaymentStatus.PENDING);
        verify(accommodationRepository).findById(ACCOMMODATION_ID);
        verify(bookingMapper).intoModel(requestDto);
        verify(bookingRepository).save(booking);
        verify(bookingMapper).intoDto(booking);
    }

    @Test
    @DisplayName("Create booking with pending payments.")
    void createBooking_PendingPayments_ThrowsIllegalStateException() {
        // Given: User with pending payments
        Payment pendingPayment = new Payment();
        when(paymentRepository.findByBookingUserIdAndStatus(USER_ID, Payment.PaymentStatus.PENDING))
                .thenReturn(List.of(pendingPayment));

        // When/Then: Verify that an exception is thrown
        assertThrows(IllegalStateException.class,
                () -> bookingService.createBooking(requestDto, currentUser));

        // Verify: No further interactions occur
        verify(paymentRepository).findByBookingUserIdAndStatus(
                USER_ID, Payment.PaymentStatus.PENDING);
        verify(accommodationRepository, never()).findById(any());
        verify(bookingRepository,
                never()).findMaxOccupancy(any(), any(), any(), any(), any(), any());
        verify(bookingMapper, never()).intoModel(any());
        verify(bookingRepository, never()).save(any());
        verify(bookingNotificationUtil, never()).notifyBookingCreated(any());
    }

    @Test
    @DisplayName("Update booking details by owner with available accommodation.")
    void updateBookingDetails_ValidUserAndAvailableAccommodation_ReturnsBookingResponseDto() {
        // Given: Booking exists, belongs to the current user, and accommodation is available
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(bookingRepository.findMaxOccupancy(
                ACCOMMODATION_ID,
                requestDto.getCheckInDate(),
                requestDto.getCheckOutDate(),
                BOOKING_ID,
                Booking.BookingStatus.CANCELED.name(),
                Booking.BookingStatus.EXPIRED.name()
        )).thenReturn(0);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.intoDto(booking)).thenReturn(responseDto);

        // When: Call the service method
        BookingResponseDto actual =
                bookingService.updateBookingDetails(BOOKING_ID, requestDto, currentUser);

        // Then: Verify the result and interactions
        assertThat(actual).isEqualTo(responseDto);
        verify(bookingRepository).findById(BOOKING_ID);
        verify(bookingMapper).updateModelFromDto(booking, requestDto);
        verify(bookingRepository).save(booking);
        verify(bookingMapper).intoDto(booking);
    }

    @Test
    @DisplayName("Update booking details with unavailable accommodation.")
    void updateBookingDetails_UnavailableAccommodation_ThrowsAccommodationNotAvailableException() {
        // Given: Booking exists, belongs to the current user, but accommodation is not available
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(bookingRepository.findMaxOccupancy(
                ACCOMMODATION_ID,
                requestDto.getCheckInDate(),
                requestDto.getCheckOutDate(),
                BOOKING_ID,
                Booking.BookingStatus.CANCELED.name(),
                Booking.BookingStatus.EXPIRED.name()
        )).thenReturn(accommodation.getAvailability());

        // When/Then: Verify that an exception is thrown
        assertThrows(AccommodationNotAvailableException.class,
                () -> bookingService.updateBookingDetails(BOOKING_ID, requestDto, currentUser));

        // Verify: No further interactions occur after the exception
        verify(bookingRepository).findById(BOOKING_ID);
        verify(bookingMapper, never()).updateModelFromDto(any(), any());
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).intoDto(any());
    }

    @Test
    @DisplayName("Cancel booking by owner successfully.")
    void cancelBooking_ValidUser_SuccessfullyCancelsBooking() {
        // Given: Booking exists and belongs to the current user
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        // When: Call the service method
        bookingService.cancelBooking(BOOKING_ID, currentUser);

        // Then: Verify the booking status is updated and notifications are sent
        assertThat(booking.getStatus()).isEqualTo(Booking.BookingStatus.CANCELED);
        verify(bookingRepository).findById(BOOKING_ID);
        verify(bookingRepository).save(booking);
        verify(bookingNotificationUtil).notifyBookingCancelled(booking, accommodation);
        verify(accommodationNotificationUtil).notifyAccommodationReleased(accommodation);
    }

    @Test
    @DisplayName("Cancel booking by non-owner.")
    void cancelBooking_NonOwner_ThrowsAccessDeniedException() {
        // Given: Booking exists but belongs to a different user
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        // When/Then: Verify that an exception is thrown
        assertThrows(AccessDeniedException.class,
                () -> bookingService.cancelBooking(BOOKING_ID, differentUser));

        // Verify: No further interactions occur after the exception
        verify(bookingRepository).findById(BOOKING_ID);
        verify(bookingRepository, never()).save(any());
        verify(bookingNotificationUtil, never()).notifyBookingCancelled(any(), any());
        verify(accommodationNotificationUtil, never()).notifyAccommodationReleased(any());
    }

    private BookingRequestDto initializeBookingRequestDto() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setAccommodationId(ACCOMMODATION_ID);
        dto.setCheckInDate(LocalDate.of(2025, 5, 1));
        dto.setCheckOutDate(LocalDate.of(2025, 5, 5));
        return dto;
    }

    private User initializeUser(Long userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }

    private Accommodation initializeAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(ACCOMMODATION_ID);
        accommodation.setAvailability(5);
        return accommodation;
    }

    private Booking initializeBooking(BookingRequestDto dto) {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setAccommodation(accommodation);
        booking.setUser(currentUser);
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setStatus(STATUS_PENDING);
        return booking;
    }

    private BookingResponseDto initializeBookingResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setAccommodationId(ACCOMMODATION_ID);
        dto.setUserId(USER_ID);
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setStatus(booking.getStatus());
        return dto;
    }
}
