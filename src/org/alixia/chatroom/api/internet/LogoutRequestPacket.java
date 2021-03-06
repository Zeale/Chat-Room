package org.alixia.chatroom.api.internet;

import java.io.Serializable;
import java.util.UUID;

public class LogoutRequestPacket implements Serializable {
	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;
	public final String username;
	public final UUID sessionID;

	public LogoutRequestPacket(final String username, final UUID sessionID) {
		this.username = username;
		this.sessionID = sessionID;
	}
}
