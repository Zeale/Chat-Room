package org.alixia.chatroom.api.internet.authmethods.exceptions;

public class AuthenticationException extends Exception {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public AuthenticationException() {
	}

	public AuthenticationException(final String message) {
		super(message);
	}

	public AuthenticationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public AuthenticationException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AuthenticationException(final Throwable cause) {
		super(cause);
	}

}
