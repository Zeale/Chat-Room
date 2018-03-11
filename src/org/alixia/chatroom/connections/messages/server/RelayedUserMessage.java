package org.alixia.chatroom.connections.messages.server;

import org.alixia.chatroom.connections.messages.client.UserMessage;
import org.alixia.chatroom.texts.BasicUserText;
import org.alixia.chatroom.texts.ConsoleText;

public class RelayedUserMessage extends UserMessage {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public final String author, message, accountName;

	public RelayedUserMessage(final String author, final String message, final String accountName) {
		this.author = author;
		this.message = message;
		this.accountName = accountName;
	}

	@Override
	public ConsoleText toConsoleText() {
		return new BasicUserText(author, message, accountName);
	}
}
