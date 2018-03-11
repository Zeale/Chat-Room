package org.alixia.chatroom.internet;

import java.io.Serializable;
import java.util.UUID;

public class SessionIDPacket implements Serializable {
	public enum Success {
		USERNAME_NOT_FOUND, WRONG_PASSWORD, SUCCESS;
	}

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;
	public final UUID sessionID;

	public final Success success;

	public SessionIDPacket(final UUID sessionID, final Success success) {
		this.sessionID = sessionID;
		this.success = success;
	}

}
