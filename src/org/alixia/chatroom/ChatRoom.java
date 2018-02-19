package org.alixia.chatroom;

import java.util.ArrayList;
import java.util.List;

import org.alixia.chatroom.connections.Client;
import org.alixia.chatroom.connections.Server;
import org.alixia.chatroom.texts.BasicInfoText;
import org.alixia.chatroom.texts.Println;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class ChatRoom {
	private final static Background DEFAULT_NODE_BACKGROUND = new Background(
			new BackgroundFill(new Color(0.4, 0.4, 0.4, 0.7), null, null));

	private List<Client> clients = new ArrayList<>();
	private List<Server> servers = new ArrayList<>();

	// Some of these fields have aliases.
	private final TextFlow flow = new TextFlow(), output = flow, console = flow;
	private final TextArea input = new TextArea();
	private final Button sendButton = new Button("Send");
	private final AnchorPane root = new AnchorPane(flow, input, sendButton), pane = root;

	private final Scene scene = new Scene(root);
	private final Stage stage;

	ChatRoom(Stage stage) {
		this.stage = stage;

		stage.setWidth(800);
		stage.setHeight(600);
		stage.show();

		flow.setBackground(DEFAULT_NODE_BACKGROUND);
		input.setBackground(DEFAULT_NODE_BACKGROUND);

		AnchorPane.setLeftAnchor(flow, 50d);
		AnchorPane.setRightAnchor(flow, 50d);
		AnchorPane.setTopAnchor(flow, 0d);
		AnchorPane.setBottomAnchor(flow, 250d);

		AnchorPane.setBottomAnchor(input, 0d);
		AnchorPane.setLeftAnchor(input, 0d);
		AnchorPane.setRightAnchor(input, 0d);

		AnchorPane.setRightAnchor(sendButton, 50d);
		AnchorPane.setBottomAnchor(sendButton, 88.5);

		input.setMaxHeight(200);

		// Add a ScrollPane to wrap flow
		root.setBackground(DEFAULT_NODE_BACKGROUND);

		stage.setScene(scene);

		try {
			tryInit();
		} catch (Exception e) {
			Text error = new Text("An error occurred.");
			error.setFont(Font.font(30));
			error.setFill(Color.CRIMSON);
			flow.setTextAlignment(TextAlignment.CENTER);
			flow.getChildren().add(error);

		}
	}

	private void tryInit() {
		input.setOnKeyPressed(event -> {
			if (event.getCode().equals(KeyCode.ENTER)) {
				onUserSubmit();
				event.consume();
			}
		});
		sendButton.setOnAction(event -> {
			onUserSubmit();
			event.consume();
		});

	}
	private void onUserSubmit() {
		input.setText("");
	}

}
