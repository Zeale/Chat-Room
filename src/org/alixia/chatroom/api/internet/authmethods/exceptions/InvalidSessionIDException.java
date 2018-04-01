package org.alixia.chatroom.api.internet.authmethods.exceptions;

public class InvalidSessionIDException extends AuthenticationException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public InvalidSessionIDException() {
	}

	public InvalidSessionIDException(final String message) {
		super(message);
	}

	public InvalidSessionIDException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public InvalidSessionIDException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidSessionIDException(final Throwable cause) {
		super(cause);
	}

}
