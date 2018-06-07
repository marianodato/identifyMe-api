package webserver.exception

class NotFoundException extends WrapperException {

    def NotFoundException(message, error = "not_found", cause = []) {
        super(message, error, cause)
    }

    def status = 404
}