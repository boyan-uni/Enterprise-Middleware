package uk.ac.newcastle.enterprisemiddleware.review;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import uk.ac.newcastle.enterprisemiddleware.restaurant.Restaurant;
import uk.ac.newcastle.enterprisemiddleware.restaurant.RestaurantService;
import uk.ac.newcastle.enterprisemiddleware.user.User;
import uk.ac.newcastle.enterprisemiddleware.user.UserService;

import javax.inject.Inject;
import java.util.Calendar;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestHTTPEndpoint(ReviewRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
class ReviewRestServiceIntegrationTest {

    private static Review review;
    private static User user;
    private static Restaurant restaurant;

    @Inject
    UserService userService;
    @Inject
    RestaurantService restaurantService;

    @BeforeAll
    static void setup() {
    }

    @Test
    @Order(1)
    public void testCanCreateReview() {
        user = userService.findById(2L);
        restaurant = restaurantService.findById(2L);

        review = new Review();
        review.setUser(user);
        review.setRestaurant(restaurant);
        review.setReview("Great!");
        review.setRating(5);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(review)
                .when()
                .post();

        // Print response in case of failure for debugging
        if (response.getStatusCode() != 201) {
            System.out.println("Response: " + response.asString());
        }

        response.then().statusCode(201);
    }

    @Test
    @Order(2)
    public void testCanGetReviews() {
        Response response = when().
                get().
                then().
                statusCode(200).
                extract().response();

        Review[] result = response.body().as(Review[].class);

        assertEquals(2, result.length);
        // assertTrue(review.getReview().equals(result[0].getReview()), "Review not equal");
        // assertTrue(review.getRating() == result[0].getRating(), "Rating not equal");
    }

    @Test
    @Order(3)
    public void testDuplicateReviewCausesError() {
        given().
                contentType(ContentType.JSON).
                body(review).
                when().
                post().
                then().
                statusCode(409).
                body("reasons.review", containsString("A review for this user and restaurant already exists"));
    }

    @Test
    @Order(4)
    public void testCanDeleteReview() {
        Response response = when().
                get().
                then().
                statusCode(200).
                extract().response();

        Review[] result = response.body().as(Review[].class);

        when().
                delete(result[0].getId().toString()).
                then().
                statusCode(204);
    }
}
