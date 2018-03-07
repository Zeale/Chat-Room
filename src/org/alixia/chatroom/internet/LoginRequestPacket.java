package org.alixia.chatroom.internet;

import java.io.Serializable;

public class LoginRequestPacket implements Serializable {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;
	public final String username, password;

	public LoginRequestPacket(String username, String password) {
		this.username = username;
		this.password = password;
	}

}
