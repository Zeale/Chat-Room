package org.alixia.chatroom.internet;

import java.io.Serializable;
import java.util.UUID;

public class LogoutRequestPacket implements Serializable {
	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;
	public final String username;
	public final UUID sessionID;

	public LogoutRequestPacket(String username, UUID sessionID) {
		this.username = username;
		this.sessionID = sessionID;
	}
}
