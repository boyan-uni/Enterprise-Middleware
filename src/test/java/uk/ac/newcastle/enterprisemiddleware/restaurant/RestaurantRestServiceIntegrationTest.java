package uk.ac.newcastle.enterprisemiddleware.restaurant;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Calendar;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestHTTPEndpoint(RestaurantRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
class RestaurantRestServiceIntegrationTest {

    private static Restaurant restaurant;

    @BeforeAll
    static void setup() {
        restaurant = new Restaurant();
        restaurant.setName("TestRestaurant");
        restaurant.setPhoneNumber("01234567890");
        restaurant.setPostcode("AB123C");
    }

    @Test
    @Order(1)
    public void testCanCreateRestaurant() {
        given().
                contentType(ContentType.JSON).
                body(restaurant).
                when().
                post().
                then().
                statusCode(201);
    }

    @Test
    @Order(2)
    public void testCanGetRestaurants() {
        Response response = when().
                get().
                then().
                statusCode(200).
                extract().response();

        Restaurant[] result = response.body().as(Restaurant[].class);

        assertEquals(1, result.length);
        assertTrue(restaurant.getName().equals(result[0].getName()), "Name not equal");
        assertTrue(restaurant.getPhoneNumber().equals(result[0].getPhoneNumber()), "Phone number not equal");
        assertTrue(restaurant.getPostcode().equals(result[0].getPostcode()), "Postcode not equal");
    }

    @Test
    @Order(3)
    public void testDuplicatePhoneNumberCausesError() {
        given().
                contentType(ContentType.JSON).
                body(restaurant).
                when().
                post().
                then().
                statusCode(409).
                body("reasons.phoneNumber", containsString("phone number is already used"));
    }

    @Test
    @Order(4)
    public void testCanDeleteRestaurant() {
        Response response = when().
                get().
                then().
                statusCode(200).
                extract().response();

        Restaurant[] result = response.body().as(Restaurant[].class);

        when().
                delete(result[0].getId().toString()).
                then().
                statusCode(204);
    }


    @Test
    @Order(5)
    public void testInvalidRestaurantCreation() {
        Restaurant invalidRestaurant = new Restaurant();
        invalidRestaurant.setName("&^");            // Invalid name
        invalidRestaurant.setPhoneNumber("123");    // Invalid phone number
        invalidRestaurant.setPostcode("12345");     // Invalid postcode

        given().
                contentType(ContentType.JSON).
                body(invalidRestaurant).
                when().
                post().
                then().
                statusCode(400).
                body("reasons.name", containsString("Please use a name without numbers or specials")).
                body("reasons.phoneNumber", containsString("Please use a vaild phoneNumber")).
                body("reasons.postcode", containsString("Postcode size must be 6"));
    }
}
