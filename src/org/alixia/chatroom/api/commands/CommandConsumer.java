package org.alixia.chatroom.api.commands;

public abstract class CommandConsumer {

	public abstract void consume(String command, String... args);

}
