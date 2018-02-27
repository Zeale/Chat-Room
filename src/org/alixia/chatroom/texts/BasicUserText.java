package org.alixia.chatroom.texts;

import org.alixia.chatroom.api.Console;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
public class BasicUserText extends ConsoleText {

	public String username, message;

	public BasicUserText(String username, String message) {
		this.username = username;
		this.message = message;
	}

	@Override
	public void print(Console console) {

		Text name = new Text(username), arrow = new Text(" > "), msg = new Text(message);
		formatText(name, arrow, msg);

		boolean specialName = username.equals("Kröw") || username.equals("Zeale");
		name.setFill(specialName ? Color.DARKGRAY : Color.RED);
		if (specialName)
			name.setFont(Font.font("Brush Script MT", DEFAULT_SIZE + 8));

		arrow.setFill(Color.WHITE);
		msg.setFill(Color.BLUE);
		console.printAll(name, arrow, msg, println());

	}

}
