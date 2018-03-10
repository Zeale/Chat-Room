package org.alixia.chatroom.texts;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.Console;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;

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

	public BasicUserText(String username, String message, String accountName) {
		this.username = username;
		this.message = message;
		this.accountName = accountName;
	}

	@Override
	public void print(Console console) {

		Text name = new Text(username), arrow = new Text(" > "), msg = new Text(message);
		formatText(name, arrow, msg);

		if (accountName != null) {
			boolean specialName = accountName.equals("Kröw") || accountName.equals("Zeale");
			name.setFill(specialName ? Color.DARKGRAY : Color.RED);
			if (specialName)
				name.setFont(Font.font("Brush Script MT", DEFAULT_SIZE + 8));

			new Object() {
				private Popup popup = new Popup();
				private Label label = new Label(accountName);

				{
					label.setTextFill(Color.ORANGERED);
					label.setBackground(null);
					popup.getScene().setRoot(label);

					popup.setWidth(label.getMaxWidth());
					label.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

					popup.setHeight(25);

					name.setOnMouseMoved(event -> {
						popup.setX(event.getScreenX());
						popup.setY(event.getScreenY() - 50);
						popup.show(ChatRoom.INSTANCE.getGUI().stage);
					});

					name.setOnMouseExited(event -> popup.hide());
				}
			};

		}

		arrow.setFill(Color.WHITE);
		msg.setFill(Color.BLUE);
		console.printAll(name, arrow, msg, println());

	}

}
