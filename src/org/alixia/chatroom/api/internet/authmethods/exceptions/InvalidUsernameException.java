package org.alixia.chatroom.api.internet.authmethods.exceptions;

public class InvalidUsernameException extends AuthenticationException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public InvalidUsernameException() {
	}

	public InvalidUsernameException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidUsernameException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidUsernameException(String message) {
		super(message);
	}

	public InvalidUsernameException(Throwable cause) {
		super(cause);
	}

}
