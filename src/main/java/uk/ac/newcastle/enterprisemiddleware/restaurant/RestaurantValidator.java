package uk.ac.newcastle.enterprisemiddleware.restaurant;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class RestaurantValidator {

    @Inject
    Validator validator;

    @Inject
    RestaurantRepository restaurantRepository;

    void validateRestaurant(Restaurant restaurant) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Restaurant>> violations = validator.validate(restaurant);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the phone number
        if (phoneNumberAlreadyExists(restaurant.getPhoneNumber(), restaurant.getId())) {
            throw new UniquePhoneNumberException("Unique Phone Number Violation");
        }
    }

    boolean phoneNumberAlreadyExists(String phoneNumber, Long id) {
        Restaurant restaurant = null;
        Restaurant restaurantWithID = null;
        try {
            restaurant = restaurantRepository.findByPhoneNumber(phoneNumber);
        } catch (NoResultException e) {
            // ignore
        }

        if (restaurant != null && id != null) {
            try {
                restaurantWithID = restaurantRepository.findById(id);
                if (restaurantWithID != null && restaurantWithID.getPhoneNumber().equals(phoneNumber)) {
                    restaurant = null;
                }
            } catch (NoResultException e) {
                // ignore
            }
        }
        return restaurant != null;
    }
}
