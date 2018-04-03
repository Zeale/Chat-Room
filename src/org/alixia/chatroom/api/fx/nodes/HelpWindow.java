package org.alixia.chatroom.api.fx.nodes;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.Printable;

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

		stage.setWidth(600);
		stage.setHeight(600);

		stage.setAlwaysOnTop(true);

		stage.getScene().setFill(ChatRoom.DEFAULT_WINDOW_BACKGROUND_COLOR);

		stage.focusedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
			if (!newValue && oldValue)
				stage.close();
		});

		Rectangle right = new Rectangle(5, 15), left = new Rectangle(5, 15);
		right.setRotate(45);
		left.setRotate(-45);

		closeButton.getChildren().addAll(right, left);
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
