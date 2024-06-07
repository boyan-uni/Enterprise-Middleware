package uk.ac.newcastle.enterprisemiddleware.user;

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
public class UserService {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    UserValidator validator;

    @Inject
    UserRepository userRepository;

    List<User> findAllOrderedByName() {
        return userRepository.findAllOrderedByName();
    }

    public User findById(Long id) {
        return userRepository.findById(id);
    }

    User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    User create(User user) throws Exception {
        log.info("UserService.create() - Creating " + user.getName());

        // Validate the user data
        validator.validateUser(user);

        // Write the user to the database
        return userRepository.create(user);
    }

    User update(User user) throws Exception {
        log.info("UserService.update() - Updating " + user.getName());

        // Validate the user data
        validator.validateUser(user);

        // Update the user in the database
        return userRepository.update(user);
    }

    User delete(User user) throws Exception {
        log.info("UserService.delete() - Deleting " + user.getName());

        User deletedUser = null;

        if (user.getId() != null) {
            deletedUser = userRepository.delete(user);
        } else {
            log.info("UserService.delete() - No ID was found so can't Delete.");
        }

        return deletedUser;
    }
}

