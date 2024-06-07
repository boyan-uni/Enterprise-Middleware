package uk.ac.newcastle.enterprisemiddleware.user;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.resteasy.reactive.Cache;
import uk.ac.newcastle.enterprisemiddleware.area.InvalidAreaCodeException;
import uk.ac.newcastle.enterprisemiddleware.util.RestServiceException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserRestService {
    @Inject
    @Named("logger")
    Logger log;

    @Inject
    UserService service;

    @GET
    @Operation(summary = "Fetch all Users", description = "Returns a JSON array of all stored User objects.")
    public Response retrieveAllUsers() {
        List<User> users = service.findAllOrderedByName();
        return Response.ok(users).build();
    }

    @GET
    @Path("/{id:[0-9]+}")
    @Operation(
            summary = "Fetch a User by id",
            description = "Returns a JSON representation of the User object with the provided id."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "User found"),
            @APIResponse(responseCode = "404", description = "User with id not found")
    })
    public Response retrieveUserById(
            @Parameter(description = "Id of User to be fetched", required = true)
            @Schema(minimum = "0", required = true)
            @PathParam("id") long id) {

        User user = service.findById(id);
        if (user == null) {
            throw new RestServiceException("No User with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }
        log.info("findById " + id + ": found User = " + user);

        return Response.ok(user).build();
    }

    @GET
    @Path("/email/{email:.+[%40|@].+}")
    @Operation(
            summary = "Fetch a User by Email",
            description = "Returns a JSON representation of the User object with the provided email."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "User found"),
            @APIResponse(responseCode = "404", description = "User with email not found")
    })
    public Response retrieveUserByEmail(
            @Parameter(description = "Email of User to be fetched", required = true)
            @PathParam("email") String email) {

        User user;
        try {
            user = service.findByEmail(email);
        } catch (NoResultException e) {
            throw new RestServiceException("No User with the email " + email + " was found!", Response.Status.NOT_FOUND);
        }
        return Response.ok(user).build();
    }

    @POST
    @Operation(description = "Add a new User to the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "User created successfully."),
            @APIResponse(responseCode = "400", description = "Invalid User supplied in request body"),
            @APIResponse(responseCode = "409", description = "User supplied in request body conflicts with an existing User"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response createUser(
            @Parameter(description = "JSON representation of User object to be added to the database", required = true)
            User user) {

        if (user == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder;

        try {
            // Clear the ID if accidentally set
            user.setId(null);

            // Go add the new User.
            service.create(user);

            // Create a "Resource Created" 201 Response and pass the user back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(user);

        } catch (ConstraintViolationException ce) {
            Map<String, String> responseObj = new HashMap<>();
            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniqueEmailException e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("email", "That email is already used, please use a unique email");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);

        } catch (Exception e) {
            throw new RestServiceException(e);
        }

        log.info("createUser completed. User = " + user);
        return builder.build();
    }

    @PUT
    @Path("/{id:[0-9]+}")
    @Operation(description = "Update a User in the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "User updated successfully"),
            @APIResponse(responseCode = "400", description = "Invalid User supplied in request body"),
            @APIResponse(responseCode = "404", description = "User with id not found"),
            @APIResponse(responseCode = "409", description = "User details supplied in request body conflict with another existing User"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response updateUser(
            @Parameter(description = "Id of User to be updated", required = true)
            @Schema(minimum = "0")
            @PathParam("id") long id,
            @Parameter(description = "JSON representation of User object to be updated in the database", required = true)
            User user) {

        if (user == null || user.getId() == null) {
            throw new RestServiceException("Invalid User supplied in request body", Response.Status.BAD_REQUEST);
        }

        if (user.getId() != null && user.getId() != id) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("id", "The User ID in the request body must match that of the User being updated");
            throw new RestServiceException("User details supplied in request body conflict with another User",
                    responseObj, Response.Status.CONFLICT);
        }

        if (service.findById(user.getId()) == null) {
            throw new RestServiceException("No User with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        Response.ResponseBuilder builder;

        try {
            service.update(user);
            builder = Response.ok(user);

        } catch (ConstraintViolationException ce) {
            Map<String, String> responseObj = new HashMap<>();
            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniqueEmailException e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("email", "That email is already used, please use a unique email");
            throw new RestServiceException("User details supplied in request body conflict with another User",
                    responseObj, Response.Status.CONFLICT, e);

        } catch (Exception e) {
            throw new RestServiceException(e);
        }

        log.info("updateUser completed. User = " + user);
        return builder.build();
    }

    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(description = "Delete a User from the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "The user has been successfully deleted"),
            @APIResponse(responseCode = "400", description = "Invalid User id supplied"),
            @APIResponse(responseCode = "404", description = "User with id not found"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response deleteUser(
            @Parameter(description = "Id of User to be deleted", required = true)
            @Schema(minimum = "0")
            @PathParam("id") long id) {

        Response.ResponseBuilder builder;

        User user = service.findById(id);
        if (user == null) {
            throw new RestServiceException("No User with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(user);
            builder = Response.noContent();

        } catch (Exception e) {
            throw new RestServiceException(e);
        }
        log.info("deleteUser completed. User = " + user);
        return builder.build();
    }
}
