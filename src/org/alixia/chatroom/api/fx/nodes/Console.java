package org.alixia.chatroom.api.fx.nodes;

import org.alixia.chatroom.api.Printable;
import org.alixia.chatroom.api.fx.tools.FXTools;
import org.alixia.chatroom.api.guis.ChatRoomWindow;
import org.alixia.chatroom.api.texts.BasicInfoText;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

public class Console extends AnchorPane implements org.alixia.chatroom.api.Console, Printable {

	private final TextFlow console = new TextFlow();
	private final ScrollPane scrollWrapper = new ScrollPane(console);

	{

		getChildren().add(scrollWrapper);

		scrollWrapper.getStylesheets().add("/org/alixia/chatroom/stylesheet.css");
		scrollWrapper.setFitToHeight(true);
		scrollWrapper.setFitToWidth(true);

		Background transparentBackground = FXTools.getBackgroundFromColor(Color.TRANSPARENT);
		setBackground(transparentBackground);
		scrollWrapper.setBackground(transparentBackground);

		console.setBackground(FXTools.getBackgroundFromColor(ChatRoomWindow.DEFAULT_NODE_OUTPUT_COLOR));

		AnchorPane.setBottomAnchor(scrollWrapper, 0d);
		AnchorPane.setTopAnchor(scrollWrapper, 0d);
		AnchorPane.setLeftAnchor(scrollWrapper, 0d);
		AnchorPane.setRightAnchor(scrollWrapper, 0d);

		setTextAlignment(TextAlignment.CENTER);

	}

	public final void setTextAlignment(TextAlignment value) {
		console.setTextAlignment(value);
	}

	@Override
	public void print(String text, Color color) {
		new BasicInfoText(text, color).print(this);
	}

	@Override
	public void printText(Text text) {
		console.getChildren().add(text);
	}

}
