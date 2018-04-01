package org.alixia.chatroom.api.internet;

import java.io.Serializable;

public class LogoutReplyPacket implements Serializable {

	public enum ErrorType {
		USERNAME_NOT_FOUND, INVALID_SESSION_ID;
	}

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public final ErrorType error;

	public LogoutReplyPacket(final ErrorType error) {
		this.error = error;
	}

	public boolean isSuccessful() {
		return error == null;
	}

}
