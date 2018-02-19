package org.alixia.chatroom;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class Launch extends Application {

	private TextFlow flow = new TextFlow();
	private TextArea input = new TextArea();
	private final static Background DEFAULT_NODE_BACKGROUND = new Background(
			new BackgroundFill(new Color(0.4, 0.4, 0.4, 0.7), null, null));

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setWidth(800);
		primaryStage.setHeight(600);
		primaryStage.show();

		flow.setBackground(DEFAULT_NODE_BACKGROUND);
		input.setBackground(DEFAULT_NODE_BACKGROUND);

		AnchorPane.setLeftAnchor(flow, 50d);
		AnchorPane.setRightAnchor(flow, 50d);
		AnchorPane.setTopAnchor(flow, 0d);
		AnchorPane.setBottomAnchor(flow, 250d);

		AnchorPane.setBottomAnchor(input, 0d);
		AnchorPane.setLeftAnchor(input, 0d);
		AnchorPane.setRightAnchor(input, 0d);

		input.setMaxHeight(200);

		// Add a ScrollPane to wrap flow
		AnchorPane root = new AnchorPane(flow, input);
		root.setBackground(DEFAULT_NODE_BACKGROUND);

		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		try {
			new ChatRoom(root, flow, input, primaryStage);
		} catch (Exception e) {
			Text error = new Text("An error occurred.");
			error.setFont(Font.font(30));
			error.setFill(Color.CRIMSON);
			flow.setTextAlignment(TextAlignment.CENTER);
			flow.getChildren().add(error);
		}
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

}
