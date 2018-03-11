package org.alixia.chatroom.internet;

import java.io.Serializable;

public class LogoutReplyPacket implements Serializable {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public final ErrorType error;

	public LogoutReplyPacket(ErrorType error) {
		this.error = error;
	}

	public boolean isSuccessful() {
		return error == null;
	}

	public enum ErrorType {
		USERNAME_NOT_FOUND, INVALID_SESSION_ID;
	}

}
