package org.alixia.chatroom.api.internet.authmethods.exceptions;

public class UnknownAuthenticationException extends RuntimeException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public UnknownAuthenticationException() {
	}

	public UnknownAuthenticationException(final String message) {
		super(message);
	}

	public UnknownAuthenticationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public UnknownAuthenticationException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnknownAuthenticationException(final Throwable cause) {
		super(cause);
	}

}
