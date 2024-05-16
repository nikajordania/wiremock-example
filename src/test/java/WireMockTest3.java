import com.github.tomakehurst.wiremock.WireMockServer;
import org.hamcrest.Matchers;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;

public class WireMockTest3 {

    WireMockServer wireMockServer;

    @BeforeMethod
    public void setup() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        setupStub();
    }

    @AfterMethod
    public void teardown() {
        wireMockServer.stop();
    }


    public void setupStub() {
        wireMockServer.stubFor(get(urlPathEqualTo("/endpoint"))
                .withQueryParam("param", equalTo("1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"key1\": \"value1\"}")));

        wireMockServer.stubFor(get(urlPathEqualTo("/endpoint"))
                .withQueryParam("param", equalTo("2"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"key2\": \"value2\"}")));

        wireMockServer.stubFor(post(urlPathEqualTo("/soap/service"))
                .withHeader("Content-Type", equalTo("text/xml; charset=UTF-8"))
                .withRequestBody(equalTo("<employeeId>1</employeeId>"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/xml; charset=UTF-8")
                        .withStatus(200)
                        .withBody("""
                                <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                                   <SOAP-ENV:Header/>
                                   <SOAP-ENV:Body>
                                      <ns2:getEmployeeByIdResponse xmlns:ns2="http://interfaces.soap.springboot.example.com">
                                         <ns2:employeeInfo>
                                            <ns2:employeeId>1</ns2:employeeId>
                                            <ns2:name>nika</ns2:name>
                                            <ns2:department>s7</ns2:department>
                                            <ns2:phone>1345678</ns2:phone>
                                            <ns2:address>bleecker street</ns2:address>
                                            <ns2:salary>1000.00</ns2:salary>
                                            <ns2:email>n@gmail.com</ns2:email>
                                            <ns2:birthDate>1900-10-15</ns2:birthDate>
                                         </ns2:employeeInfo>
                                      </ns2:getEmployeeByIdResponse>
                                   </SOAP-ENV:Body>
                                </SOAP-ENV:Envelope>""")));
    }

    @Test
    public void testQueryParam1() {
        given()
                .queryParam("param", "1")
                .when()
                .get("http://localhost:8089/endpoint")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType("application/json")
                .body("key1", Matchers.equalTo("value1"));
    }

    @Test
    public void testQueryParam2() {
        given()
                .queryParam("param", "2")
                .when()
                .get("http://localhost:8089/endpoint")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType("application/json")
                .body("key2", Matchers.equalTo("value2"));
    }

    @Test
    public void testSOAPService() {
        given()
                .body("<employeeId>1</employeeId>")
                .when()
                .contentType("text/xml; charset=UTF-8")
                .post("http://localhost:8089/soap/service").then().log().all()
                .assertThat()
                .statusCode(200)
                .contentType("text/xml; charset=UTF-8")
                .body("Envelope.Body.getEmployeeByIdResponse.employeeInfo.employeeId", Matchers.equalTo("1"))
                .body("Envelope.Body.getEmployeeByIdResponse.employeeInfo.name", Matchers.equalTo("nika"))
                .body("Envelope.Body.getEmployeeByIdResponse.employeeInfo.department", Matchers.equalTo("s7"))
                .body("Envelope.Body.getEmployeeByIdResponse.employeeInfo.phone", Matchers.equalTo("1345678"))
                .body("Envelope.Body.getEmployeeByIdResponse.employeeInfo.address", Matchers.equalTo("bleecker street"))
                .body("Envelope.Body.getEmployeeByIdResponse.employeeInfo.salary", Matchers.equalTo("1000.00"))
                .body("Envelope.Body.getEmployeeByIdResponse.employeeInfo.email", Matchers.equalTo("n@gmail.com"))
                .body("Envelope.Body.getEmployeeByIdResponse.employeeInfo.birthDate", Matchers.equalTo("1900-10-15"));
    }
}
