package org.alixia.chatroom.commands;


public abstract class CommandConsumer {

	public abstract void consume(String command, String...args);
	
	
}
