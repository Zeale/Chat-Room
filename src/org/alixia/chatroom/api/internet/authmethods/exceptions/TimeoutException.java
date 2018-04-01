package org.alixia.chatroom.api.internet.authmethods.exceptions;

public class TimeoutException extends AuthenticationException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public TimeoutException() {
	}

	public TimeoutException(final String message) {
		super(message);
	}

	public TimeoutException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public TimeoutException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TimeoutException(final Throwable cause) {
		super(cause);
	}

}
