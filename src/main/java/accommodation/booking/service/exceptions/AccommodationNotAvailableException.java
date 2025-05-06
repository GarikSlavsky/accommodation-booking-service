package accommodation.booking.service.exceptions;

public class AccommodationNotAvailableException extends RuntimeException {
    public AccommodationNotAvailableException(String message) {
        super(message);
    }
}
