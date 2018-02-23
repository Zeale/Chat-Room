package org.alixia.chatroom.resources.fxnodes.popbutton;

import javafx.scene.Node;
import javafx.scene.control.Button;

public class PopButton extends Button {
	{
		getStylesheets().add("/org/alixia/chatroom/resources/fxnodes/popbutton/PopButton.css");
		getStyleClass().add("pop-button");
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
