package uk.ac.newcastle.enterprisemiddleware.review;

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


@Path("/reviews")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ReviewRestService {
    @Inject
    @Named("logger")
    Logger log;

    @Inject
    ReviewService service;

    @GET
    @Operation(summary = "Fetch all Reviews", description = "Returns a JSON array of all stored Review objects.")
    public Response retrieveAllReviews() {
        List<Review> reviews = service.findAll();
        return Response.ok(reviews).build();
    }

    @GET
    @Path("/{id:[0-9]+}")
    @Operation(
            summary = "Fetch a Review by id",
            description = "Returns a JSON representation of the Review object with the provided id."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Review found"),
            @APIResponse(responseCode = "404", description = "Review with id not found")
    })
    public Response retrieveReviewById(
            @Parameter(description = "Id of Review to be fetched", required = true)
            @Schema(minimum = "0", required = true)
            @PathParam("id") long id) {

        Review review = service.findById(id);
        if (review == null) {
            throw new RestServiceException("No Review with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }
        log.info("findById " + id + ": found Review = " + review);

        return Response.ok(review).build();
    }

    @GET
    @Path("/user/{userId:[0-9]+}")
    @Operation(
            summary = "Fetch Reviews by User ID",
            description = "Returns a JSON array of Review objects for the specified User ID."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Reviews found"),
            @APIResponse(responseCode = "404", description = "No reviews found for the given user ID")
    })
    public Response retrieveReviewsByUserId(
            @Parameter(description = "User ID for which reviews are to be fetched", required = true)
            @PathParam("userId") long userId) {

        List<Review> reviews = service.findByUserId(userId);
        if (reviews.isEmpty()) {
            throw new RestServiceException("No reviews found for the user ID " + userId, Response.Status.NOT_FOUND);
        }
        return Response.ok(reviews).build();
    }

    @POST
    @Operation(description = "Add a new Review to the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Review created successfully."),
            @APIResponse(responseCode = "400", description = "Invalid Review supplied in request body"),
            @APIResponse(responseCode = "409", description = "Review supplied in request body conflicts with an existing Review"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response createReview(
            @Parameter(description = "JSON representation of Review object to be added to the database", required = true)
            Review review) {

        if (review == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder;

        try {
            // Clear the ID if accidentally set
            review.setId(null);

            // Go add the new Review.
            service.create(review);

            // Create a "Resource Created" 201 Response and pass the review back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(review);

        } catch (ConstraintViolationException ce) {
            Map<String, String> responseObj = new HashMap<>();
            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniqueReviewException e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("review", "A review for this user and restaurant already exists");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);

        } catch (Exception e) {
            throw new RestServiceException(e);
        }

        log.info("createReview completed. Review = " + review);
        return builder.build();
    }

    @PUT
    @Path("/{id:[0-9]+}")
    @Operation(description = "Update a Review in the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Review updated successfully"),
            @APIResponse(responseCode = "400", description = "Invalid Review supplied in request body"),
            @APIResponse(responseCode = "404", description = "Review with id not found"),
            @APIResponse(responseCode = "409", description = "Review details supplied in request body conflict with another existing Review"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response updateReview(
            @Parameter(description = "Id of Review to be updated", required = true)
            @Schema(minimum = "0")
            @PathParam("id") long id,
            @Parameter(description = "JSON representation of Review object to be updated in the database", required = true)
            Review review) {

        if (review == null || review.getId() == null) {
            throw new RestServiceException("Invalid Review supplied in request body", Response.Status.BAD_REQUEST);
        }

        if (review.getId() != null && review.getId() != id) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("id", "The Review ID in the request body must match that of the Review being updated");
            throw new RestServiceException("Review details supplied in request body conflict with another Review",
                    responseObj, Response.Status.CONFLICT);
        }

        if (service.findById(review.getId()) == null) {
            throw new RestServiceException("No Review with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        Response.ResponseBuilder builder;

        try {
            service.update(review);
            builder = Response.ok(review);

        } catch (ConstraintViolationException ce) {
            Map<String, String> responseObj = new HashMap<>();
            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniqueReviewException e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("review", "A review for this user and restaurant already exists");
            throw new RestServiceException("Review details supplied in request body conflict with another Review",
                    responseObj, Response.Status.CONFLICT, e);

        } catch (Exception e) {
            throw new RestServiceException(e);
        }

        log.info("updateReview completed. Review = " + review);
        return builder.build();
    }

    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(description = "Delete a Review from the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "The review has been successfully deleted"),
            @APIResponse(responseCode = "400", description = "Invalid Review id supplied"),
            @APIResponse(responseCode = "404", description = "Review with id not found"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response deleteReview(
            @Parameter(description = "Id of Review to be deleted", required = true)
            @Schema(minimum = "0")
            @PathParam("id") long id) {

        Response.ResponseBuilder builder;

        Review review = service.findById(id);
        if (review == null) {
            throw new RestServiceException("No Review with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(review);
            builder = Response.noContent();

        } catch (Exception e) {
            throw new RestServiceException(e);
        }
        log.info("deleteReview completed. Review = " + review);
        return builder.build();
    }
}
