package uk.ac.newcastle.enterprisemiddleware.review;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ReviewValidator {

    @Inject
    Validator validator;

    @Inject
    ReviewRepository reviewRepository;

    void validateReview(Review review) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Review>> violations = validator.validate(review);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the review by user and restaurant
        if (reviewAlreadyExists(review.getUser().getId(), review.getRestaurant().getId())) {
            throw new UniqueReviewException("Unique Review Violation");
        }
    }

    boolean reviewAlreadyExists(Long userId, Long restaurantId) {
        try {
            List<Review> reviews = reviewRepository.findByUserId(userId);
            for (Review review : reviews) {
                if (review.getRestaurant().getId().equals(restaurantId)) {
                    return true;
                }
            }
        } catch (NoResultException e) {
            // ignore
        }
        return false;
    }
}
