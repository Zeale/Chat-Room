package org.alixia.chatroom.internet.authmethods.exceptions;

public class IncorrectPasswordException extends AuthenticationException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public IncorrectPasswordException() {
	}

	public IncorrectPasswordException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IncorrectPasswordException(String message, Throwable cause) {
		super(message, cause);
	}

	public IncorrectPasswordException(String message) {
		super(message);
	}

	public IncorrectPasswordException(Throwable cause) {
		super(cause);
	}

}
