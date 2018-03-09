package org.alixia.chatroom.internet;

import java.io.Serializable;
import java.util.UUID;

public class SessionIDPacket implements Serializable {
	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;
	public final UUID sessionID;
	public final Success success;

	public enum Success {
		USERNAME_NOT_FOUND, WRONG_PASSWORD, SUCCESS;
	}

	public SessionIDPacket(UUID sessionID, Success success) {
		this.sessionID = sessionID;
		this.success = success;
	}

}
