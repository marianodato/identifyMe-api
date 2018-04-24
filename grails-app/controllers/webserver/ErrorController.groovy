package webserver

import org.codehaus.groovy.grails.web.errors.GrailsWrappedRuntimeException
import webserver.exception.WrapperException

class ErrorController {

    def handleError() {
        def resp = [:]
        try{
            def exception = request.exception
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
            }

            log.error("Status: " + resp.status)
            log.error("Message: " + resp.message)
            log.error("Error: " + resp.error)
            log.error("Cause: " + resp.cause)

            return [status: resp.status, response: resp]

        }catch(Throwable e) {
            return [status: 500, response: resp]
        }
    }

    def notFound() {
        def resp = [:]
        resp.message = "Resource $request.forwardURI not found."
        resp.error = "not_found"
        resp.status = 404
        resp.cause = []
        return [status: 404, response: resp]
    }
}