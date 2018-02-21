package org.alixia.chatroom.commands;

import java.util.Stack;

public abstract class CommandConsumer {
	private final Stack<String> commands = new Stack<>();

	public abstract void consume(String command, String...args);
}
