package dev.vepo.engage.comments.list;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ListVideoCommentsEndpointTest {

    @Test
    void shouldRequireAuthenticationForVideoComments() {
        given().when()
               .get("/api/videos/1/comments")
               .then()
               .statusCode(401);
    }

    @Test
    void shouldRequireAuthenticationForChannelComments() {
        given().when()
               .get("/api/channels/1/comments")
               .then()
               .statusCode(401);
    }
}
