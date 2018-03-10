package org.alixia.chatroom.connections.messages.client.requests;

import java.io.Serializable;

public class NameChangeRequest implements Serializable {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public final String newName;

	public NameChangeRequest(String newName) {
		this.newName = newName;
	}

}
