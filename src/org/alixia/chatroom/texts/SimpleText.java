package org.alixia.chatroom.texts;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class SimpleText extends ConsoleText {

	public final Text text = new Text();

	{
		formatText(text);
	}

	@Override
	public void print(TextFlow flow) {
		flow.getChildren().add(text);
	}

}
