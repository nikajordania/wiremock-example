import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;

public class WireMockTest2 {

    WireMockServer wireMockServer;

    @BeforeTest
    public void setup() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        wireMockServer.start();
        RestAssured.baseURI = "http://127.0.0.1";
        RestAssured.port = 8089;
        setupStub();

    }

    @AfterTest
    public void teardown() {
        wireMockServer.stop();
    }

    public void setupStub() {
        configureFor("127.0.0.1", 8089);
        stubFor(get(urlEqualTo("/some/thing"))
                .withHeader("Accept", matching("text/plain"))
                .willReturn(aResponse().
                        withStatus(503).
                        withHeader("Content-Type", "text/html").
                        withBody("Service Not Available"))
        );

        stubFor(get(urlEqualTo("/some/thing"))
                .withHeader("Accept", matching("application/json"))
                .willReturn(aResponse().
                        withStatus(200).
                        withHeader("Content-Type", "application/json")
                        .withBody("{\"serviceStatus\": \"running\"}")
                        .withFixedDelay(2500))
        );
    }

    @Test
    public void test1() {

        Response r = given()
                .header(new Header("Accept", "text/plain"))
                .when()
                .get("/some/thing");
        Assert.assertEquals(r.statusCode(), HttpStatus.SC_SERVICE_UNAVAILABLE);
    }


    @Test
    public void test2() {

        Response r = given()
                .header(new Header("Accept", "application/json"))
                .when()
                .get("/some/thing");
        Assert.assertEquals(r.statusCode(), HttpStatus.SC_OK);
        Assert.assertEquals(r.jsonPath().getString("serviceStatus"), "running");
    }

    @Test
    public void test3() {

        Response r = given()
                .header(new Header("Accept", "application/json"))
                .when()
                .get("/some/thing/is/wrong");
        Assert.assertEquals(r.statusCode(), HttpStatus.SC_NOT_FOUND);
    }
}
