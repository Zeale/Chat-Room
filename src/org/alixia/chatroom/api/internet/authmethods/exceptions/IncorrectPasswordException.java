package org.alixia.chatroom.api.internet.authmethods.exceptions;

public class IncorrectPasswordException extends AuthenticationException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public IncorrectPasswordException() {
	}

	public IncorrectPasswordException(final String message) {
		super(message);
	}

	public IncorrectPasswordException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public IncorrectPasswordException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IncorrectPasswordException(final Throwable cause) {
		super(cause);
	}

}
