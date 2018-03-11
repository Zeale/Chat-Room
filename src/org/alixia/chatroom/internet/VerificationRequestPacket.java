package org.alixia.chatroom.internet;

import java.io.Serializable;
import java.util.UUID;

public class VerificationRequestPacket implements Serializable {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;
	public final String username;
	public final UUID sessionID;// I think one UUID should be fine for now ;)

	public VerificationRequestPacket(final String username, final UUID sessionID) {
		this.username = username;
		this.sessionID = sessionID;
	}

}
