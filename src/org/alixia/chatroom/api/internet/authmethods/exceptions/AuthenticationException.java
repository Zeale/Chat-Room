package org.alixia.chatroom.api.internet.authmethods.exceptions;

public class AuthenticationException extends Exception {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public AuthenticationException() {
	}

	public AuthenticationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthenticationException(String message) {
		super(message);
	}

	public AuthenticationException(Throwable cause) {
		super(cause);
	}

}