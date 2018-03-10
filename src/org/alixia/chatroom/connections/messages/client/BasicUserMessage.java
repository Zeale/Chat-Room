package org.alixia.chatroom.connections.messages.client;

import org.alixia.chatroom.connections.messages.Message;

public class BasicUserMessage extends Message {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public final String text;

	public BasicUserMessage(String text) {
		this.text = text;
	}

}
