package webserver.exception

class AuthException extends WrapperException {

    def status = 401

    def AuthException(message, error, cause, status) {
        super(message, error, cause, status)
    }

    def AuthException(message, error = "unauthorized", cause = []) {
        super(message, error, cause)
    }
}