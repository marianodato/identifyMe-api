package webserver

import grails.converters.JSON
import org.codehaus.groovy.grails.web.errors.GrailsWrappedRuntimeException
import webserver.exception.WrapperException

class ErrorController {

    def handleError() {
        def resp = [:]
        try{
            def exception = request.exception
            response.status = 500
            resp.message = "Oops! Something went wrong..."
            resp.error = "internal_error"
            resp.status = 500
            resp.cause = []

            if (exception instanceof GrailsWrappedRuntimeException) {
                while (exception instanceof GrailsWrappedRuntimeException) {
                    exception = exception.cause
                }
            }

            if (exception instanceof WrapperException) {
                resp.message = exception.hasProperty("message") ? exception.message : resp.message
                resp.error = exception.hasProperty("error") ? exception.error : resp.error
                resp.status = exception.hasProperty("status") ? exception.status : resp.status
                resp.cause = exception.hasProperty("exceptionCause") ? exception.exceptionCause : resp.cause
                response.status = exception.hasProperty("status") ? exception.status : resp.status
            }

            log.error("Status: " + resp.status)
            log.error("Message: " + resp.message)
            log.error("Error: " + resp.error)
            log.error("Cause: " + resp.cause)

            render resp as JSON

        }catch(Throwable e) {
            render resp as JSON
        }
    }

    def notFound() {
        def resp = [:]
        log.error("Resource $request.forwardURI not found.")
        resp.message = "Resource $request.forwardURI not found."
        resp.error = "not_found"
        resp.status = 404
        resp.cause = []
        response.status = 404
        render resp as JSON
    }
}