package org.alixia.chatroom.api;

import javafx.scene.paint.Color;

public interface Printable {
	void print(String text, Color color);

	default void println() {
		print("\n", Color.WHITE);
	}

	default void println(final String text, final Color color) {
		print(text, color);
		println();
	}
}
