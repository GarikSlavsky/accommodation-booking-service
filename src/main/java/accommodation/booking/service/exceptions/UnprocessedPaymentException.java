package accommodation.booking.service.exceptions;

public class UnprocessedPaymentException extends RuntimeException {
    public UnprocessedPaymentException(String message) {
        super(message);
    }
}
