package org.alixia.chatroom.api.connections.messages.client;

import org.alixia.chatroom.api.connections.messages.Message;

public class BasicUserMessage extends Message {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public final String text;

	public BasicUserMessage(final String text) {
		this.text = text;
	}

}
