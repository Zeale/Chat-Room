package org.alixia.chatroom.api;

import javafx.scene.text.Text;

public interface Console {
	public default void printAll(final Text... texts) {
		for (final Text t : texts)
			printText(t);
	}

	public void printText(Text text);
}
