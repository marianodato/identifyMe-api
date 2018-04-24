package webserver.exception

class WrapperException extends RuntimeException {

    def status = 500
    def error
    def exceptionCause = []

    def WrapperException(message, error, cause, status) {
        super(message.toString(), (cause in Throwable) ? cause : null)
        this.error = error
        this.exceptionCause = cause
        this.status = status
    }

    def WrapperException(message, error, cause) {
        super(message.toString(), (cause in Throwable) ? cause : null)
        this.error = error
        this.exceptionCause = cause
    }

    def WrapperException(message){
        super(message)
    }

    def WrapperException() {
        super("internal_error")
    }

}
