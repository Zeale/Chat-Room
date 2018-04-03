package org.alixia.chatroom.api.fx.nodes;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.Printable;
import org.alixia.chatroom.api.fx.tools.FXTools;

import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class HelpWindow implements org.alixia.chatroom.api.Console, Printable {

	private final Stage stage = new Stage(StageStyle.TRANSPARENT);
	private final Console console = new Console();

	public void print(String text, Color color) {
		console.print(text, color);
	}

	public void printText(Text text) {
		console.printText(text);
	}

	public HelpWindow() {
	}

	private final StackPane closeButton = new StackPane();
	private final AnchorPane root = new AnchorPane(console, closeButton);

	{
		stage.setScene(new Scene(root));

		stage.setWidth(400);
		stage.setHeight(400);

		stage.setAlwaysOnTop(true);

		stage.getScene().setFill(ChatRoom.DEFAULT_WINDOW_BACKGROUND_COLOR);

		stage.focusedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
			if (!newValue && oldValue) {
				if (stage.getOwner() != null)
					stage.getOwner().requestFocus();
				stage.close();
			}
		});

		double length = 3.6, width = 15;
		Rectangle right = new Rectangle(length, width), left = new Rectangle(length, width);
		right.setRotate(45);
		left.setRotate(-45);

		closeButton.getChildren().addAll(right, left);

		root.setBackground(FXTools.getBackgroundFromColor(ChatRoom.DEFAULT_WINDOW_BACKGROUND_COLOR));
		AnchorPane.setBottomAnchor(console, 25d);
		AnchorPane.setLeftAnchor(console, 25d);
		AnchorPane.setRightAnchor(console, 25d);
		AnchorPane.setTopAnchor(console, 25d);

		AnchorPane.setLeftAnchor(closeButton, 9d);
		AnchorPane.setTopAnchor(closeButton, 5d);

	}

	public void setOwner(Window owner) {
		stage.initOwner(owner);
	}

	void show() {
		stage.show();
	}

	void hide() {
		stage.hide();
	}

	public boolean isShowing() {
		return stage.isShowing();
	}
}
