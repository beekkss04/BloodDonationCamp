/** Our own exception. We throw it when a donor, appointment or donation is not valid. */
public class InvalidDonationException extends Exception {
    public InvalidDonationException(String message) {
        super(message);
    }
}
