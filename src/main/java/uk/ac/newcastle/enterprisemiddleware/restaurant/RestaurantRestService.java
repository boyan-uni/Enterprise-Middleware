package uk.ac.newcastle.enterprisemiddleware.restaurant;

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

@Path("/restaurants")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RestaurantRestService {
    @Inject
    @Named("logger")
    Logger log;

    @Inject
    RestaurantService service;

    @GET
    @Operation(summary = "Fetch all Restaurants", description = "Returns a JSON array of all stored Restaurant objects.")
    public Response retrieveAllRestaurants() {
        List<Restaurant> restaurants = service.findAllOrderedByName();
        return Response.ok(restaurants).build();
    }

    @GET
    @Path("/{id:[0-9]+}")
    @Operation(
            summary = "Fetch a Restaurant by id",
            description = "Returns a JSON representation of the Restaurant object with the provided id."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Restaurant found"),
            @APIResponse(responseCode = "404", description = "Restaurant with id not found")
    })
    public Response retrieveRestaurantById(
            @Parameter(description = "Id of Restaurant to be fetched", required = true)
            @Schema(minimum = "0", required = true)
            @PathParam("id") long id) {

        Restaurant restaurant = service.findById(id);
        if (restaurant == null) {
            throw new RestServiceException("No Restaurant with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }
        log.info("findById " + id + ": found Restaurant = " + restaurant);

        return Response.ok(restaurant).build();
    }

    @POST
    @Operation(description = "Add a new Restaurant to the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Restaurant created successfully."),
            @APIResponse(responseCode = "400", description = "Invalid Restaurant supplied in request body"),
            @APIResponse(responseCode = "409", description = "Restaurant supplied in request body conflicts with an existing Restaurant"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response createRestaurant(
            @Parameter(description = "JSON representation of Restaurant object to be added to the database", required = true)
            Restaurant restaurant) {

        if (restaurant == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder;

        try {
            // Clear the ID if accidentally set
            restaurant.setId(null);

            // Go add the new Restaurant.
            service.create(restaurant);

            // Create a "Resource Created" 201 Response and pass the restaurant back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(restaurant);

        } catch (ConstraintViolationException ce) {
            Map<String, String> responseObj = new HashMap<>();
            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniquePhoneNumberException e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("phoneNumber", "That phone number is already used, please use a unique phone number");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);

        } catch (Exception e) {
            throw new RestServiceException(e);
        }

        log.info("createRestaurant completed. Restaurant = " + restaurant);
        return builder.build();
    }

    @PUT
    @Path("/{id:[0-9]+}")
    @Operation(description = "Update a Restaurant in the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Restaurant updated successfully"),
            @APIResponse(responseCode = "400", description = "Invalid Restaurant supplied in request body"),
            @APIResponse(responseCode = "404", description = "Restaurant with id not found"),
            @APIResponse(responseCode = "409", description = "Restaurant details supplied in request body conflict with another existing Restaurant"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response updateRestaurant(
            @Parameter(description = "Id of Restaurant to be updated", required = true)
            @Schema(minimum = "0")
            @PathParam("id") long id,
            @Parameter(description = "JSON representation of Restaurant object to be updated in the database", required = true)
            Restaurant restaurant) {

        if (restaurant == null || restaurant.getId() == null) {
            throw new RestServiceException("Invalid Restaurant supplied in request body", Response.Status.BAD_REQUEST);
        }

        if (restaurant.getId() != null && restaurant.getId() != id) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("id", "The Restaurant ID in the request body must match that of the Restaurant being updated");
            throw new RestServiceException("Restaurant details supplied in request body conflict with another Restaurant",
                    responseObj, Response.Status.CONFLICT);
        }

        if (service.findById(restaurant.getId()) == null) {
            throw new RestServiceException("No Restaurant with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        Response.ResponseBuilder builder;

        try {
            service.update(restaurant);
            builder = Response.ok(restaurant);

        } catch (ConstraintViolationException ce) {
            Map<String, String> responseObj = new HashMap<>();
            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniquePhoneNumberException e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("phoneNumber", "That phone number is already used, please use a unique phone number");
            throw new RestServiceException("Restaurant details supplied in request body conflict with another Restaurant",
                    responseObj, Response.Status.CONFLICT, e);

        } catch (Exception e) {
            throw new RestServiceException(e);
        }

        log.info("updateRestaurant completed. Restaurant = " + restaurant);
        return builder.build();
    }

    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(description = "Delete a Restaurant from the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "The restaurant has been successfully deleted"),
            @APIResponse(responseCode = "400", description = "Invalid Restaurant id supplied"),
            @APIResponse(responseCode = "404", description = "Restaurant with id not found"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response deleteRestaurant(
            @Parameter(description = "Id of Restaurant to be deleted", required = true)
            @Schema(minimum = "0")
            @PathParam("id") long id) {

        Response.ResponseBuilder builder;

        Restaurant restaurant = service.findById(id);
        if (restaurant == null) {
            throw new RestServiceException("No Restaurant with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(restaurant);
            builder = Response.noContent();

        } catch (Exception e) {
            throw new RestServiceException(e);
        }
        log.info("deleteRestaurant completed. Restaurant = " + restaurant);
        return builder.build();
    }
}
