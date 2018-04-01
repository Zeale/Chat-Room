package org.alixia.chatroom.api.internet;

import java.io.Serializable;

public class CreateAccountRequestPacket implements Serializable {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;
	public final String name;
	public final String password;

	public CreateAccountRequestPacket(final String name, final String password) {
		this.name = name;
		this.password = password;
	}

}
