package org.alixia.chatroom.api.internet.authmethods.exceptions;

public class InvalidUsernameException extends AuthenticationException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public InvalidUsernameException() {
	}

	public InvalidUsernameException(final String message) {
		super(message);
	}

	public InvalidUsernameException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public InvalidUsernameException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidUsernameException(final Throwable cause) {
		super(cause);
	}

}
