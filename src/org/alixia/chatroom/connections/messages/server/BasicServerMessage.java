package org.alixia.chatroom.connections.messages.server;

import org.alixia.chatroom.texts.ConsoleText;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class BasicServerMessage extends ServerMessage {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;
	public String message;

	public BasicServerMessage(String message) {
		this.message = message;
	}

	@Override
	public ConsoleText toConsoleText() {
		return new ConsoleText() {

			@Override
			public void print(TextFlow flow) {
				Text server = new Text("[SERVER]: ");
				Text message = new Text(BasicServerMessage.this.message);
				flow.getChildren().addAll(server, message);
			}
		};
	}

}
