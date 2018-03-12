package org.alixia.chatroom.api.internet;

import java.io.Serializable;

public class LoginRequestPacket implements Serializable {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;
	public final String username, password;

	public LoginRequestPacket(final String username, final String password) {
		this.username = username;
		this.password = password;
	}

}
