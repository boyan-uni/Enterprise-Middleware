package uk.ac.newcastle.enterprisemiddleware.review;

import javax.validation.ValidationException;

/**
 * <p>ValidationException caused if a Review for the same User and Restaurant already exists.</p>
 *
 * <p>This violates the uniqueness constraint.</p>
 */
public class UniqueReviewException extends ValidationException {

    public UniqueReviewException(String message) {
        super(message);
    }

    public UniqueReviewException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniqueReviewException(Throwable cause) {
        super(cause);
    }
}

