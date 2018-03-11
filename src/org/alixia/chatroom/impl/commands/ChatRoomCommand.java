package org.alixia.chatroom.impl.commands;

import static org.alixia.chatroom.ChatRoom.INFO_COLOR;
import static org.alixia.chatroom.ChatRoom.SUCCESS_COLOR;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.commands.Command;

import javafx.scene.paint.Color;

abstract class ChatRoomCommand extends Command {

	{
		// This is basically the only line that makes this class "belong" in the impl
		// package. Otherwise, it's an API class.
		//
		// I suppose this class could be split up into an API class (that goes somewhere
		// else) and this implementation class (which would extend the API class) but
		// they'd have such small amounts of code that it'd be pointless.
		ChatRoom.INSTANCE.commandManager.addCommand(this);
	}

	protected void printHelp(final String usage, final String... descriptions) {
		ChatRoom.INSTANCE.print("Usage: " + usage, INFO_COLOR);
		ChatRoom.INSTANCE.print(" - ", Color.WHITE);
		for (final String s : descriptions)
			ChatRoom.INSTANCE.println(s, SUCCESS_COLOR);
	}

}
