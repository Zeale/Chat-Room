package org.alixia.chatroom.resources.fxnodes.popbutton;

import org.alixia.chatroom.api.fx.tools.FXTools;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

public class PopButton extends Button {
	{
		getStylesheets().add("/org/alixia/chatroom/resources/fxnodes/popbutton/PopButton.css");
		getStyleClass().add("pop-button");
		setFill(new Color(0, 0, 0, 21d / 255));
	}

	public PopButton() {
		super();
	}

	public PopButton(final String text) {
		super(text);
	}

	public PopButton(final String text, final Node graphic) {
		super(text, graphic);
	}

	public void setFill(final Color color) {
		setBackground(FXTools.getBackgroundFromColor(color));
	}

}
