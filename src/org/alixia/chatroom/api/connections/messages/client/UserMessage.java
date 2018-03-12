package org.alixia.chatroom.api.connections.messages.client;

import org.alixia.chatroom.api.connections.messages.Message;
import org.alixia.chatroom.api.texts.ConsoleText;

public abstract class UserMessage extends Message {
	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public abstract ConsoleText toConsoleText();
}
