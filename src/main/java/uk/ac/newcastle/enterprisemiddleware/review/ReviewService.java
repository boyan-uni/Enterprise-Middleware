package uk.ac.newcastle.enterprisemiddleware.review;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import uk.ac.newcastle.enterprisemiddleware.area.Area;
import uk.ac.newcastle.enterprisemiddleware.area.AreaService;
import uk.ac.newcastle.enterprisemiddleware.area.InvalidAreaCodeException;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;

@Dependent
public class ReviewService {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    ReviewValidator validator;

    @Inject
    ReviewRepository reviewRepository;

    List<Review> findAll() {
        return reviewRepository.findAll();
    }

    List<Review> findByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    List<Review> findByRestaurantId(Long restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId);
    }

    Review findById(Long id) {
        return reviewRepository.findById(id);
    }

    Review create(Review review) throws Exception {
        log.info("ReviewService.create() - Creating review for restaurant " + review.getRestaurant().getName() + " by user " + review.getUser().getName());

        // Validate the review data
        validator.validateReview(review);

        // Write the review to the database
        return reviewRepository.create(review);
    }

    Review update(Review review) throws Exception {
        log.info("ReviewService.update() - Updating review for restaurant " + review.getRestaurant().getName() + " by user " + review.getUser().getName());

        // Validate the review data
        validator.validateReview(review);

        // Update the review in the database
        return reviewRepository.update(review);
    }

    Review delete(Review review) throws Exception {
        log.info("ReviewService.delete() - Deleting review for restaurant " + review.getRestaurant().getName() + " by user " + review.getUser().getName());

        Review deletedReview = null;

        if (review.getId() != null) {
            deletedReview = reviewRepository.delete(review);
        } else {
            log.info("ReviewService.delete() - No ID was found so can't Delete.");
        }

        return deletedReview;
    }
}

