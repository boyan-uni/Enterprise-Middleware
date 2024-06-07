package uk.ac.newcastle.enterprisemiddleware.user;


import javax.validation.ValidationException;

/**
 * <p>ValidationException caused if a User's email address conflicts with that of another User.</p>
 *
 * <p>This violates the uniqueness constraint.</p>
 */
public class UniqueEmailException extends ValidationException {

    public UniqueEmailException(String message) {
        super(message);
    }

    public UniqueEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniqueEmailException(Throwable cause) {
        super(cause);
    }
}

