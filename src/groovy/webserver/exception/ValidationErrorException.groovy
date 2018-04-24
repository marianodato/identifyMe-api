package webserver.exception

class ValidationErrorException extends WrapperException {

    def ValidationErrorException(message, error = "Validation error", cause = []) {
        super(message, error, cause)
    }

    def status = 409
}