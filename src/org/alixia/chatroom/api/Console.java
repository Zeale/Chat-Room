package org.alixia.chatroom.api;

import javafx.scene.text.Text;

public interface Console {
	public void printText(Text text);

	public default void printAll(Text... texts) {
		for (Text t : texts)
			printText(t);
	}
}
