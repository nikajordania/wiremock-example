
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;

public class WireMockTest {

    WireMockServer wireMockServer;

    @BeforeMethod
    public void setup() {
        wireMockServer = new WireMockServer(8090);
        wireMockServer.start();
        setupStub();
    }

    @AfterMethod
    public void teardown() {
        wireMockServer.stop();
    }

    public void setupStub() {
        wireMockServer.stubFor(get(urlEqualTo("/an/endpoint"))
                .willReturn(aResponse().withHeader("Content-Type", "text/plain")
                        .withStatus(200)
                        .withBodyFile("json/glossary.json")));
    }

    @Test
    public void testStatusCodePositive() {
        given().
                when().
                get("http://localhost:8090/an/endpoint").
                then().log().all().
                assertThat().statusCode(200);
    }

    @Test
    public void testStatusCodeNegative() {
        given().
                when().
                get("http://localhost:8090/another/endpoint").
                then().
                assertThat().statusCode(404);
    }

    @Test
    public void testResponseContents() {
        Response response = given().when().get("http://localhost:8090/an/endpoint");
        String title = response.jsonPath().get("glossary.title");
        System.out.println(title);
        Assert.assertEquals("example glossary", title);
    }
}
