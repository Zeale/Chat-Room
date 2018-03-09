package org.alixia.chatroom.api;

import java.io.Serializable;
import java.util.UUID;

public class UserData implements Serializable {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public final String username;
	public final UUID sessionID;

	public UserData(String username, UUID sessionID) {
		this.username = username;
		this.sessionID = sessionID;
	}

}
