package webserver.exception

class ServiceUnavailableException extends WrapperException {
    def status = 503

    def ServiceUnavailableException(message, error = "service_unavailable", cause = [[code: "SUE", description: "Service unavailable"]]) {
        super(message, error, cause)
    }
}
