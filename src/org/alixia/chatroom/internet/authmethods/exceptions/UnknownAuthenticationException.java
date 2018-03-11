package org.alixia.chatroom.internet.authmethods.exceptions;

public class UnknownAuthenticationException extends RuntimeException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public UnknownAuthenticationException() {
	}

	public UnknownAuthenticationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnknownAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownAuthenticationException(String message) {
		super(message);
	}

	public UnknownAuthenticationException(Throwable cause) {
		super(cause);
	}

}
