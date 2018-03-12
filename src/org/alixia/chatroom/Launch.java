package org.alixia.chatroom;

import org.alixia.chatroom.api.texts.BoldText;
import org.alixia.chatroom.impl.commands.Commands;

import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Launch extends Application {

	public final static long STARTUP_TIME = System.currentTimeMillis();

	public static void main(final String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		new ChatRoom();
		ChatRoom.INSTANCE.setStage(primaryStage);

		ChatRoom.INSTANCE.println("Setting up commands...", Color.BISQUE);

		ChatRoom.INSTANCE.print("Commands took ", ChatRoom.SUCCESS_COLOR);
		final long initialTime = System.currentTimeMillis();
		new BoldText(Commands.getTime() - initialTime + " ", Color.FIREBRICK).print(ChatRoom.INSTANCE.console);
		ChatRoom.INSTANCE.println("milliseconds to load!", ChatRoom.SUCCESS_COLOR);

	}

}
