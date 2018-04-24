package webserver.exception

class BadRequestException extends WrapperException {
    def status = 400

    def BadRequestException(message, error = "bad_request", cause = []) {
        super(message, error, cause)
    }
}
