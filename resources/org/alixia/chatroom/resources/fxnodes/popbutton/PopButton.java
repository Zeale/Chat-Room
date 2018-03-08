package org.alixia.chatroom.resources.fxnodes.popbutton;

import org.alixia.chatroom.fxtools.FXTools;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

public class PopButton extends Button {
	{
		getStylesheets().add("/org/alixia/chatroom/resources/fxnodes/popbutton/PopButton.css");
		getStyleClass().add("pop-button");
		setFill(new Color(0, 0, 0, 21d / 255));
	}

	public void setFill(Color color) {
		setBackground(FXTools.getBackgroundFromColor(color));
	}

	public PopButton() {
		super();
	}

	public PopButton(String text, Node graphic) {
		super(text, graphic);
	}

	public PopButton(String text) {
		super(text);
	}

}
