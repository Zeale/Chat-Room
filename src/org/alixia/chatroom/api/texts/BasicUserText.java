package org.alixia.chatroom.api.texts;

import org.alixia.chatroom.api.Console;
import org.alixia.chatroom.api.fx.tools.FXTools;

import javafx.scene.control.Label;
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

	public String username, message, accountName;

	public BasicUserText(final String username, final String message, final String accountName) {
		this.username = username;
		this.message = message;
		this.accountName = accountName;
	}

	@Override
	public void print(final Console console) {

		final Text name = new Text(username), arrow = new Text(" > "), msg = new Text(message);
		formatText(name, arrow, msg);

		if (accountName != null) {
			final boolean specialName = accountName.equals("Kröw") || accountName.equals("Zeale");
			name.setFill(specialName ? Color.DARKGRAY : Color.RED);
			if (specialName)
				name.setFont(Font.font("Brush Script MT", DEFAULT_SIZE + 8));

			Label label = FXTools.addHoverText(name, accountName, new Color(0.1, 0.1, 0.1, 0.85));
			label.setTextFill(Color.ORANGERED);
			label.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

		}

		arrow.setFill(Color.WHITE);
		msg.setFill(Color.BLUE);
		console.printAll(name, arrow, msg, println());

	}

}
