package org.alixia.chatroom.internet.authmethods;

import org.alixia.chatroom.internet.authmethods.exceptions.AuthenticationException;

public class UsernameTakenException extends AuthenticationException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public UsernameTakenException() {
	}

	public UsernameTakenException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UsernameTakenException(String message, Throwable cause) {
		super(message, cause);
	}

	public UsernameTakenException(String message) {
		super(message);
	}

	public UsernameTakenException(Throwable cause) {
		super(cause);
	}

}
