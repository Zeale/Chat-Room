package org.alixia.chatroom;

import javafx.application.Application;
import javafx.stage.Stage;

public class Launch extends Application {

	public final static long STARTUP_TIME = System.currentTimeMillis();

	@Override
	public void start(Stage primaryStage) throws Exception {
		new ChatRoom(primaryStage);
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

}
