package org.alixia.chatroom.texts;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class BasicInfoText extends ConsoleText {

	public String text;
	public Color color;

	@Override
	public void print(TextFlow flow) {
		Text text = new Text(this.text);
		formatText(text);
		text.setFill(color);
		flow.getChildren().add(text);
	}

	public BasicInfoText(String text, Color color) {
		this.text = text;
		this.color = color;
	}

}
