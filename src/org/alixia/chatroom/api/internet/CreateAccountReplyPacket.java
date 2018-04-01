package org.alixia.chatroom.api.internet;

import java.io.Serializable;
import java.util.UUID;

public class CreateAccountReplyPacket implements Serializable {
	public enum ErrType {
		USERNAME_EXISTS, REQUEST_DENIED, INVALID_USERNAME;
	}

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;
	public final UUID sessionID;

	public final ErrType error;

	public CreateAccountReplyPacket(final UUID sessionID, final ErrType error) {
		this.sessionID = sessionID;
		this.error = error;
	}
}
