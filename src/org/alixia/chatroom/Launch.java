package org.alixia.chatroom;

import javafx.application.Application;
import javafx.stage.Stage;

public class Launch extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setWidth(800);
		primaryStage.setHeight(600);
		primaryStage.show();
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

}
