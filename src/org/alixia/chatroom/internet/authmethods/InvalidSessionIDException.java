package org.alixia.chatroom.internet.authmethods;

import org.alixia.chatroom.internet.authmethods.exceptions.AuthenticationException;

public class InvalidSessionIDException extends AuthenticationException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public InvalidSessionIDException() {
	}

	public InvalidSessionIDException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidSessionIDException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidSessionIDException(String message) {
		super(message);
	}

	public InvalidSessionIDException(Throwable cause) {
		super(cause);
	}

}
