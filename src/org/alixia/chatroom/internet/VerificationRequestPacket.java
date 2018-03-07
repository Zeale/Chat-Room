package org.alixia.chatroom.internet;

import java.util.UUID;

public class VerificationRequestPacket {
	
	public final String username;
	public final UUID sessionID;// I think one UUID should be fine for now ;)

	public VerificationRequestPacket(String username, UUID sessionID) {
		this.username = username;
		this.sessionID = sessionID;
	}

}
