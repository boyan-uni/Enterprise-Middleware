package uk.ac.newcastle.enterprisemiddleware.restaurant;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import uk.ac.newcastle.enterprisemiddleware.area.Area;
import uk.ac.newcastle.enterprisemiddleware.area.AreaService;
import uk.ac.newcastle.enterprisemiddleware.area.InvalidAreaCodeException;
import uk.ac.newcastle.enterprisemiddleware.review.Review;
import uk.ac.newcastle.enterprisemiddleware.review.ReviewRepository;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;

@Dependent
public class RestaurantService {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    RestaurantValidator validator;

    @Inject
    RestaurantRepository restaurantRepository;

    @Inject
    ReviewRepository reviewRepository; // Add ReviewRepository Injection

    List<Restaurant> findAllOrderedByName() {
        return restaurantRepository.findAllOrderedByName();
    }

    public Restaurant findById(Long id) {
        return restaurantRepository.findById(id);
    }

    Restaurant create(Restaurant restaurant) throws Exception {
        log.info("RestaurantService.create() - Creating " + restaurant.getName());

        // Validate the restaurant data
        validator.validateRestaurant(restaurant);

        // Write the restaurant to the database
        return restaurantRepository.create(restaurant);
    }

    Restaurant update(Restaurant restaurant) throws Exception {
        log.info("RestaurantService.update() - Updating " + restaurant.getName());

        // Validate the restaurant data
        validator.validateRestaurant(restaurant);

        // Update the restaurant in the database
        return restaurantRepository.update(restaurant);
    }

    Restaurant delete(Restaurant restaurant) throws Exception {
        log.info("RestaurantService.delete() - Deleting " + restaurant.getName());

        Restaurant deletedRestaurant = null;

        if (restaurant.getId() != null) {
            // firstly delete all reviews associated with the restaurant
            List<Review> reviews = reviewRepository.findByRestaurantId(restaurant.getId());
            for (Review review : reviews) {
                reviewRepository.delete(review);
            }
            // secondly delete the restaurant
            deletedRestaurant = restaurantRepository.delete(restaurant);
        } else {
            log.info("RestaurantService.delete() - No ID was found so can't Delete.");
        }

        return deletedRestaurant;
    }
}

