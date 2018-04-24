package webserver.exception

class ForbiddenException extends WrapperException {

    def ForbiddenException(message, error = "forbidden", cause = []) {
        super(message, error, cause)
    }

    def status = 403
}
