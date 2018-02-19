package org.alixia.chatroom.connections.messages;

import org.alixia.chatroom.texts.BasicUserText;
import org.alixia.chatroom.texts.ConsoleText;

public class BasicUserMessage extends UserMessage {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public final String author, text;

	public BasicUserMessage(String author, String text) {
		this.author = author;
		this.text = text;
	}

	public ConsoleText toConsoleText() {
		return new BasicUserText(author, text);
	}

}
