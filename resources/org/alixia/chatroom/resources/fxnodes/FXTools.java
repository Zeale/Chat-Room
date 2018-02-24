package org.alixia.chatroom.resources.fxnodes;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

public final class FXTools {
	private FXTools() {
	}

	public static Background getBackgroundFromColor(Color color) {
		return new Background(new BackgroundFill(color, null, null));
	}
}
