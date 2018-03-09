package org.alixia.chatroom.impl.commands;

import static org.alixia.chatroom.ChatRoom.INFO_COLOR;
import static org.alixia.chatroom.ChatRoom.SUCCESS_COLOR;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.commands.Command;

import javafx.scene.paint.Color;

abstract class ChatRoomCommand extends Command {

	{
		ChatRoom.INSTANCE.commandManager.addCommand(this);
	}

	protected void printHelp(String usage, String... descriptions) {
		ChatRoom.INSTANCE.print("Usage: " + usage, INFO_COLOR);
		ChatRoom.INSTANCE.print(" - ", Color.WHITE);
		for (String s : descriptions)
			ChatRoom.INSTANCE.println(s, SUCCESS_COLOR);
	}

}
