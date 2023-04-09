package test.bmt.mdfarm.controller


import io.restassured.response.Response
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification
import spock.lang.Stepwise

import static io.restassured.RestAssured.given

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Stepwise
class MobileDevicesFarmControllerIT extends Specification {
    def USER_ID_QUERY_PARAM = "user_id"
    def MODEL_QUERY_PARAM = "model"

    @LocalServerPort
    int port

    def "perform booking"() {
        given:
        def user = "bob"
        def model = "SAMSUNG_GALAXY_S9"

        when:
        var request = given()
                .queryParam(USER_ID_QUERY_PARAM, user)
                .queryParam(MODEL_QUERY_PARAM, model)
        var response = request.post(address("v1/devices:book"))
        validateResponse(response, HttpStatus.OK)

        then:
        def list = getList()
        def md = list.find { it['model_code'] = model }
        md['booked'] == true
        md['booking_owner'] == user
        md['book_time'] != null
    }

    def "perform booking (several devices of the same model)"() {
        expect:
        var request = given()
                .queryParam(USER_ID_QUERY_PARAM, user)
                .queryParam(MODEL_QUERY_PARAM, model)
        var response = request.post(address("v1/devices:book"))

        response.then().statusCode(httpCode.value())

        response.body.path("error") == error

        where:
        user  | model               | httpCode            | error
        "bob" | "SAMSUNG_GALAXY_S8" | HttpStatus.OK       | null
        "bob" | "SAMSUNG_GALAXY_S8" | HttpStatus.OK       | null
        "bob" | "SAMSUNG_GALAXY_S8" | HttpStatus.CONFLICT | "There is no free device of type <SAMSUNG_GALAXY_S8>"
    }

    def "release by a wrong user"() {
        given:
        def user = "bobX"
        def model = "SAMSUNG_GALAXY_S9"

        when:
        var request = given()
                .queryParam(USER_ID_QUERY_PARAM, user)
                .queryParam(MODEL_QUERY_PARAM, model)
        var response = request.post(address("v1/devices:release"))
        validateResponse(response, HttpStatus.CONFLICT)

        then:
        def list = getList()
        def md = list.find { it['model_code'] = model }
        md['booked'] == true
        md['booking_owner'] != user
    }

    def "release booking"() {
        given:
        def user = "bob"
        def model = "SAMSUNG_GALAXY_S9"

        when:
        var request = given()
                .queryParam(USER_ID_QUERY_PARAM, user)
                .queryParam(MODEL_QUERY_PARAM, model)
        var response = request.post(address("v1/devices:release"))
        validateResponse(response, HttpStatus.OK)

        then:
        def list = getList()
        def md = list.find { it['model_code'] = model }
        md['booked'] == false
        md['booking_owner'] == null
        md['book_time'] == null
    }

    def List<Map> getList() {
        var request = given()
        var response = request.get(address("v1/devices"))
        validateResponse(response, HttpStatus.OK)
        return response.body.path("devices")
    }

    def address(String path) {
        "http://localhost:$port/$path"
    }

    def validateResponse(Response response, HttpStatus expectedStatus) {
        if (response.statusCode() != expectedStatus.value())
            throw new IllegalStateException("Wrong response <${generateErrorMessage(response)}>")
    }

    def generateErrorMessage(Response response) {
        [
                statusCode : response.statusCode(),
                headers    : response.getHeaders(),
                contentType: response.contentType(),
                body       : response.asPrettyString()
        ]
    }
}
