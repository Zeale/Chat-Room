package org.alixia.chatroom.guis;

import org.alixia.chatroom.resources.fxnodes.popbutton.PopButton;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SettingsWindow extends Stage {
	private final Button save = new PopButton("save"), cancel = new PopButton("cancel");
	private final VBox settingsBox = new VBox();
	private final AnchorPane root = new AnchorPane(settingsBox);
	private final Scene scene = new Scene(root);
	{
		initStyle(StageStyle.TRANSPARENT);
		root.setMinSize(600, 400);
		setScene(scene);

		scene.setFill(Color.TRANSPARENT);

		HBox buttonWrapper = new HBox(15, save, cancel);
		buttonWrapper.setAlignment(Pos.CENTER);
		AnchorPane.setBottomAnchor(buttonWrapper, 15d);
		AnchorPane.setLeftAnchor(buttonWrapper, 0d);
		AnchorPane.setRightAnchor(buttonWrapper, 0d);
		root.getChildren().add(buttonWrapper);

		focusedProperty().addListener(
				(ChangeListener<Boolean>) (observable, oldValue, newValue) -> root.setOpacity(newValue ? 1 : 0.2));
	}

}
