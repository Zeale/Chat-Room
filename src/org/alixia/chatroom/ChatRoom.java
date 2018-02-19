package org.alixia.chatroom;

import java.util.ArrayList;
import java.util.List;

import org.alixia.chatroom.connections.Client;
import org.alixia.chatroom.connections.Server;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class ChatRoom {

	private List<Client> clients = new ArrayList<>();
	private List<Server> servers = new ArrayList<>();

	private final AnchorPane root, pane;
	private final TextFlow flow, output;
	private final TextArea input;

	private final Stage stage;
	private final Scene scene;

	public ChatRoom(AnchorPane root, TextFlow flow, TextArea input, Stage stage) throws RuntimeException {
		this.root = pane = root;
		this.flow = output = flow;
		this.input = input;
		this.stage = stage;
		this.scene = stage.getScene();
	}

}
