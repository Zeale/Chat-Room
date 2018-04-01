package org.alixia.chatroom.api.internet.authmethods.exceptions;

public class UsernameTakenException extends AuthenticationException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public UsernameTakenException() {
	}

	public UsernameTakenException(final String message) {
		super(message);
	}

	public UsernameTakenException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public UsernameTakenException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UsernameTakenException(final Throwable cause) {
		super(cause);
	}

}
