package uk.ac.newcastle.enterprisemiddleware.user;

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
@TestHTTPEndpoint(UserRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
class UserRestServiceIntegrationTest {

    private static User user;

    @BeforeAll
    static void setup() {
        user = new User();
        user.setName("TestUser");
        user.setEmail("testuser@email.com");
        user.setPhoneNumber("01234567890");
    }

    @Test
    @Order(1)
    public void testCanCreateUser() {
        given().
                contentType(ContentType.JSON).
                body(user).
                when().
                post().
                then().
                statusCode(201);
    }

    @Test
    @Order(2)
    public void testCanGetUsers() {
        Response response = when().
                get().
                then().
                statusCode(200).
                extract().response();

        User[] result = response.body().as(User[].class);

        // import.sql
        assertEquals(3, result.length);
        // assertTrue(user.getName().equals(result[0].getName()), "Name not equal");
        // assertTrue(user.getEmail().equals(result[0].getEmail()), "Email not equal");
        // assertTrue(user.getPhoneNumber().equals(result[0].getPhoneNumber()), "Phone number not equal");
    }

    @Test
    @Order(3)
    public void testDuplicateEmailCausesError() {
        given().
                contentType(ContentType.JSON).
                body(user).
                when().
                post().
                then().
                statusCode(409).
                body("reasons.email", containsString("email is already used"));
    }

    @Test
    @Order(4)
    public void testCanDeleteUser() {
        Response response = when().
                get().
                then().
                statusCode(200).
                extract().response();

        User[] result = response.body().as(User[].class);

        when().
                delete(result[0].getId().toString()).
                then().
                statusCode(204);

        // Cascade Deletion finished
    }


    @Test
    @Order(5)
    public void testInvalidUserCreation() {
        User invalidUser = new User();
        invalidUser.setName("");                // Invalid name
        invalidUser.setEmail("invalidEmail");   // Invalid email
        invalidUser.setPhoneNumber("123");      // Invalid phone number

        given().
                contentType(ContentType.JSON).
                body(invalidUser).
                when().
                post().
                then().
                statusCode(400).
                body("reasons.name", containsString("Please use a name without numbers or specials")).
                body("reasons.email", containsString("The email address must be in the format of name@domain.com")).
                body("reasons.phoneNumber", containsString("Please use a vaild phoneNumber"));
    }

}

