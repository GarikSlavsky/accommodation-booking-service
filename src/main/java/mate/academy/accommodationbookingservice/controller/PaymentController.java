package mate.academy.accommodationbookingservice.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.accommodationbookingservice.dto.payment.PaymentRequestDto;
import mate.academy.accommodationbookingservice.dto.payment.PaymentResponseDto;
import mate.academy.accommodationbookingservice.model.User;
import mate.academy.accommodationbookingservice.service.payment.PaymentService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<PaymentResponseDto> getPayments(
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
    public PaymentResponseDto initiatePayment(
            @RequestBody PaymentRequestDto requestDto, Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        return paymentService.initiatePayment(requestDto.getBookingId(), currentUser);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/renew/{paymentId}")
    public PaymentResponseDto renewPaymentSession(
            @PathVariable Long paymentId, Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        return paymentService.renewPaymentSession(paymentId, currentUser);
    }

    @GetMapping("/success")
    public String handlePaymentSuccess(@RequestParam("session_id") String sessionId) {
        return paymentService.handlePaymentSuccess(sessionId);
    }

    @GetMapping("/cancel")
    public String handlePaymentCancel(@RequestParam("session_id") String sessionId) {
        return paymentService.handlePaymentCancel(sessionId);
    }
}
