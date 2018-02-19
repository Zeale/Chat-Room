package org.alixia.chatroom.connections.messages;

import org.alixia.chatroom.texts.ConsoleText;

public abstract class UserMessage extends Message {
	public abstract ConsoleText toConsoleText();
}
