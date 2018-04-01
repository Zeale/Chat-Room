package org.alixia.chatroom.api.internet.authmethods.exceptions;

public class AccountCreationDeniedException extends AuthenticationException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public AccountCreationDeniedException() {
	}

	public AccountCreationDeniedException(final String message) {
		super(message);
	}

	public AccountCreationDeniedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public AccountCreationDeniedException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AccountCreationDeniedException(final Throwable cause) {
		super(cause);
	}

}
