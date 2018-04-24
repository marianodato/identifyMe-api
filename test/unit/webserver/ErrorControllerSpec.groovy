package webserver

import grails.test.mixin.TestFor
import spock.lang.Specification
import webserver.exception.BadRequestException

@TestFor(ErrorController)
class ErrorControllerSpec extends Specification {

    void "test 404 not found"() {
        given:
        request.forwardURI = "/test/404"

        when:
        def resp = controller.notFound()

        then:
        resp.status == 404
        resp.response.equals(["cause": [], "status": 404, "error": "not_found", "message": "Resource /test/404 not found."])
    }

    void "test handle custom error"() {
        given:
        request.exception = new BadRequestException("Bad Request")

        when:
        def resp = controller.handleError()

        then:
        resp.status == 400
        resp.response.equals(["cause": [], "status": 400, "error": "bad_request", "message": "Bad Request"])
    }

    void "test handle error"() {
        given:
        request.exception = new Exception("Error example")

        when:
        def resp = controller.handleError()

        then:
        resp.status == 500
        resp.response.equals(["cause": [], "status": 500, "error": "internal_error", "message": "Oops! Something went wrong..."])
    }
}
