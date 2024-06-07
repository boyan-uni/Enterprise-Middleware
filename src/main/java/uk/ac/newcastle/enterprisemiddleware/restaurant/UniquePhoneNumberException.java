package uk.ac.newcastle.enterprisemiddleware.restaurant;

import javax.validation.ValidationException;

/**
 * <p>ValidationException caused if a Restaurant's phone number conflicts with that of another Restaurant.</p>
 *
 * <p>This violates the uniqueness constraint.</p>
 */
public class UniquePhoneNumberException extends ValidationException {

    public UniquePhoneNumberException(String message) {
        super(message);
    }

    public UniquePhoneNumberException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniquePhoneNumberException(Throwable cause) {
        super(cause);
    }
}
