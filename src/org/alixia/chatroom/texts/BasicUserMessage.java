package org.alixia.chatroom.texts;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * <p>
 * This class is reusable, but does not need to be reused. It is a very simple
 * factory.
 * <p>
 * This class is used to print formatted texts to a {@link TextFlow}
 * representing user messages.
 * 
 * @author Zeale
 *
 */
public class BasicUserMessage extends ConsoleText {
	public String username, message;

	@Override
	public void print(TextFlow flow) {
		Text name = new Text(username), arrow = new Text(" > "), msg = new Text(message);
		formatText(name, arrow, msg);
		name.setFill(Color.RED);
		arrow.setFill(Color.WHITE);
		msg.setFill(Color.BLUE);
		flow.getChildren().addAll(name, arrow, msg, println());
	}

	public BasicUserMessage(String username, String message) {
		this.username = username;
		this.message = message;
	}

}
