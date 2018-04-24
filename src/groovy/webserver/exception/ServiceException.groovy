package webserver.exception

import org.springframework.http.HttpStatus;

class ServiceException extends WrapperException {
	
		public ServiceException(String message, HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR) {
			super(message)
			this.status = status
		}
	
		public ServiceException(String message, Throwable cause, HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR) {
			super(message,"service", cause);
			this.status = this.status
		}
}
