package dev.vepo.engage.comments.wordcloud;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class VideoCommentWordCloudEndpointTest {

    @Test
    void shouldRequireAuthenticationForVideoWordCloud() {
        given().when()
               .get("/api/videos/1/comments/word-cloud")
               .then()
               .statusCode(401);
    }

    @Test
    void shouldRequireAuthenticationForChannelWordCloud() {
        given().when()
               .get("/api/channels/1/comments/word-cloud")
               .then()
               .statusCode(401);
    }
}
