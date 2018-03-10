package org.alixia.chatroom.connections.messages.client;

import org.alixia.chatroom.connections.messages.Message;
import org.alixia.chatroom.texts.ConsoleText;

public abstract class UserMessage extends Message {
	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public abstract ConsoleText toConsoleText();
}
