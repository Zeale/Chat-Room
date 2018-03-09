package org.alixia.chatroom.texts;

import org.alixia.chatroom.api.Console;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class BoldText extends ConsoleText {

	public String text;
	public Color color;

	public BoldText() {
	}

	public BoldText(Color color) {
		this.color = color;
	}

	public BoldText(String text) {
		this.text = text;
	}

	public BoldText(String text, Color color) {
		this.text = text;
		this.color = color;
	}

	@Override
	public void print(Console console) {
		Text text = new Text(this.text);
		text.setFont(Font.font(DEFAULT_FAMLIY, FontWeight.EXTRA_BOLD, DEFAULT_SIZE));
		text.setFill(color);
		console.printText(text);
	}

}
