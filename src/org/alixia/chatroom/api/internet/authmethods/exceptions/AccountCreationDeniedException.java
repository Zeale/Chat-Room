package org.alixia.chatroom.api.internet.authmethods.exceptions;

public class AccountCreationDeniedException extends AuthenticationException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public AccountCreationDeniedException() {
	}

	public AccountCreationDeniedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AccountCreationDeniedException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccountCreationDeniedException(String message) {
		super(message);
	}

	public AccountCreationDeniedException(Throwable cause) {
		super(cause);
	}

}
