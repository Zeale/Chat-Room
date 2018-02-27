package org.alixia.chatroom.texts;

import org.alixia.chatroom.api.Console;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class BasicInfoText extends ConsoleText {

	public String text;
	public Color color;

	public BasicInfoText(String text, Color color) {
		this.text = text;
		this.color = color;
	}

	@Override
	public void print(Console console) {

		Text text = new Text(this.text);
		formatText(text);
		text.setFill(color);
		console.printText(text);

	}

}
