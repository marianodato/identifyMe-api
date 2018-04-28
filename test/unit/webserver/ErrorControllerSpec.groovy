package webserver

import grails.converters.JSON
import grails.test.mixin.TestFor
import spock.lang.Specification
import webserver.exception.BadRequestException

@TestFor(ErrorController)
class ErrorControllerSpec extends Specification {

    void "test 404 not found"() {
        given:
        request.forwardURI = "/test/404"

        when:
        controller.notFound()

        then:
        response.status == 404
        controller.response.json == JSON.parse("{\"cause\":[], \"message\":\"Resource /test/404 not found.\", \"error\":\"not_found\", \"status\":404}")
    }

    void "test handle custom error"() {
        given:
        request.exception = new BadRequestException("Bad Request")

        when:
        controller.handleError()

        then:
        response.status == 400
        controller.response.json == JSON.parse("{\"cause\":[], \"message\":\"Bad Request\", \"error\":\"bad_request\", \"status\":400}")
    }

    void "test handle error"() {
        given:
        request.exception = new Exception("Error example")

        when:
        controller.handleError()

        then:
        response.status == 500
        controller.response.json == JSON.parse("{\"cause\":[], \"message\":\"Oops! Something went wrong...\", \"error\":\"internal_error\", \"status\":500}")
    }
}
