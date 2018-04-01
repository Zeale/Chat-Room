package org.alixia.chatroom.api.internet.authmethods.exceptions;

public class UsernameNotFoundException extends AuthenticationException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public UsernameNotFoundException() {
	}

	public UsernameNotFoundException(final String message) {
		super(message);
	}

	public UsernameNotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public UsernameNotFoundException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UsernameNotFoundException(final Throwable cause) {
		super(cause);
	}

}
