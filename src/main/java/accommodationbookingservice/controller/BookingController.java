package accommodationbookingservice.controller;

import accommodationbookingservice.dto.booking.BookingRequestDto;
import accommodationbookingservice.dto.booking.BookingResponseDto;
import accommodationbookingservice.dto.booking.BookingStatusPatchRequestDto;
import accommodationbookingservice.model.Booking;
import accommodationbookingservice.model.User;
import accommodationbookingservice.service.booking.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Booking management", description = "Endpoints for managing booking.")
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Permits the creation of new accommodation bookings.")
    public BookingResponseDto createBooking(@RequestBody @Valid BookingRequestDto requestDto,
                                            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return bookingService.createBooking(requestDto, currentUser);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieves bookings based on user ID and their status for managers.")
    public Page<BookingResponseDto> getBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Booking.BookingStatus status,
            Pageable pageable) {
        return bookingService.getBookings(userId, status, pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieves bookings for the currently logged-in user.")
    public Page<BookingResponseDto> getUserBookings(
            Authentication authentication, Pageable pageable) {

        User currentUser = (User) authentication.getPrincipal();
        return bookingService.getBookingsByUser(currentUser.getId(), pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Provides information about a specific booking.")
    public BookingResponseDto getBookingById(@PathVariable Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return bookingService.getBookingById(id, currentUser);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Allows customers to update booking details beyond status.")
    public BookingResponseDto updateBookingDetails(@PathVariable Long id,
                                                   @RequestBody @Valid BookingRequestDto requestDto,
                                                   Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return bookingService.updateBookingDetails(id, requestDto, currentUser);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Allows managers to update booking status alone.")
    public BookingResponseDto updateBookingStatus(
            @PathVariable Long id,
            @RequestBody @Valid BookingStatusPatchRequestDto patchRequestDto) {

        return bookingService.updateBookingStatus(id, patchRequestDto);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Enables the cancellation of bookings.")
    public void cancelBooking(@PathVariable Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        bookingService.cancelBooking(id, currentUser);
    }
}
