package dev.vepo.engage.video.list;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

@QuarkusTest
class ListVideoEndpointTest {

    @Test
    void shouldRequireAuthenticationForVideosPage() {
        given().when()
               .get("/api/videos")
               .then()
               .statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = "engage.admin")
    void shouldReturnEmptyVideoPage() {
        given().when()
               .queryParam("page", 0)
               .queryParam("size", 20)
               .get("/api/videos")
               .then()
               .statusCode(200)
               .body("items", hasSize(0))
               .body("total", equalTo(0))
               .body("page", equalTo(0))
               .body("pageSize", equalTo(20));
    }
}
