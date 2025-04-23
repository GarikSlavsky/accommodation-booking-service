package accommodation.booking.service.controller;

import accommodation.booking.service.dto.payment.PaymentRequestDto;
import accommodation.booking.service.dto.payment.PaymentResponseDto;
import accommodation.booking.service.model.User;
import accommodation.booking.service.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment management", description = "Endpoints for managing payments.")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "Retrieves payments based on user ID or the current user.")
    public Page<PaymentResponseDto> getPayments(
            @RequestParam(name = "user_id", required = false) Long userId,
            Authentication authentication,
            Pageable pageable) {

        User currentUser = (User) authentication.getPrincipal();
        if (userId == null && currentUser.getAuthorities().contains("ROLE_MANAGER")) {
            return paymentService.getPaymentsForUser(null, currentUser, pageable);
        }
        return paymentService.getPaymentsForUser(
                userId != null ? userId : currentUser.getId(),
                currentUser,
                pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Initiates a payment session for a booking.")
    public PaymentResponseDto initiatePayment(
            @RequestBody PaymentRequestDto requestDto, Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        return paymentService.initiatePayment(requestDto.getBookingId(), currentUser);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/renew/{paymentId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Renews an expired payment session.")
    public PaymentResponseDto renewPaymentSession(
            @PathVariable Long paymentId, Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        return paymentService.renewPaymentSession(paymentId, currentUser);
    }

    @GetMapping("/success")
    @Operation(summary = "Handles successful payment notifications from Stripe.")
    public String handlePaymentSuccess(@RequestParam("session_id") String sessionId) {
        return paymentService.handlePaymentSuccess(sessionId);
    }

    @GetMapping("/cancel")
    @Operation(summary = "Handles payment cancellations.")
    public String handlePaymentCancel(@RequestParam("session_id") String sessionId) {
        return paymentService.handlePaymentCancel(sessionId);
    }
}
