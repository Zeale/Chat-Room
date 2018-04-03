package org.alixia.chatroom.api.fx.nodes;

import org.alixia.chatroom.api.Printable;
import org.alixia.chatroom.api.fx.tools.FXTools;
import org.alixia.chatroom.api.guis.ChatRoomWindow;
import org.alixia.chatroom.api.texts.BasicInfoText;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Console extends TextFlow implements org.alixia.chatroom.api.Console, Printable {

	{
		setBackground(FXTools.getBackgroundFromColor(ChatRoomWindow.DEFAULT_NODE_OUTPUT_COLOR));
	}

	@Override
	public void print(String text, Color color) {
		new BasicInfoText(text, color).print(this);
	}

	@Override
	public void printText(Text text) {
		getChildren().add(text);
	}

}
