package org.alixia.chatroom.api.connections.messages;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public final UUID id;

	public Message() {
		id = UUID.randomUUID();
	}

	Message(final UUID id) {
		this.id = id;
	}

}
