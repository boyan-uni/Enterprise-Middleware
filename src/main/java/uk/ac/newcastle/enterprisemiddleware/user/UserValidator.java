package uk.ac.newcastle.enterprisemiddleware.user;

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
public class UserValidator {

    @Inject
    Validator validator;

    @Inject
    UserRepository userRepository;

    void validateUser(User user) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the email address
        if (emailAlreadyExists(user.getEmail(), user.getId())) {
            throw new UniqueEmailException("Unique Email Violation");
        }
    }

    boolean emailAlreadyExists(String email, Long id) {
        User user = null;
        User userWithID = null;
        try {
            user = userRepository.findByEmail(email);
        } catch (NoResultException e) {
            // ignore
        }

        if (user != null && id != null) {
            try {
                userWithID = userRepository.findById(id);
                if (userWithID != null && userWithID.getEmail().equals(email)) {
                    user = null;
                }
            } catch (NoResultException e) {
                // ignore
            }
        }
        return user != null;
    }
}

