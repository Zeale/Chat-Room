package org.alixia.chatroom.api.texts;

import org.alixia.chatroom.api.Console;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class BasicInfoText extends ConsoleText {

	public String text;
	public Color color;

	public BasicInfoText(final String text, final Color color) {
		this.text = text;
		this.color = color;
	}

	@Override
	public void print(final Console console) {

		final Text text = new Text(this.text);
		formatText(text);
		text.setFill(color);
		console.printText(text);

	}

}
