package webserver.exception

class InsufientStorageException extends WrapperException {
    def status = 507

    def InsufientStorageException(message, error = "insufient_storage", cause = []) {
        super(message, error, cause)
    }
}
