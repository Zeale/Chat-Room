package org.alixia.chatroom.impl.guis.settings;

import java.io.IOException;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.items.LateLoadItem;
import org.alixia.chatroom.resources.fxnodes.popbutton.PopButton;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

abstract class _SettingsWindowImpl extends Stage {

	private final Button save = new PopButton("save"), cancel = new PopButton("cancel");

	private final VBox settingsBox = new VBox();
	private final AnchorPane root = new AnchorPane();
	private LateLoadItem<_AdvancedSettingsImpl> advancedSettings = new LateLoadItem<>(
			() -> new _AdvancedSettingsImpl());
	{
		root.getChildren().addListener(new ListChangeListener<Node>() {

			@Override
			public void onChanged(Change<? extends Node> c) {
				while (c.next())
					if (c.wasAdded())
						for (Node n : c.getAddedSubList()) {
							if (n instanceof Parent) {
								// PickOnBounds is only being disabled for parent objects. I don't want to mess
								// up any buttons or anything. (Not that doing this to them will, but just to be
								// safe...)
								n.setPickOnBounds(false);
								((Parent) n).getChildrenUnmodifiable().addListener(this);
							}
						}
			}
		});
		root.getChildren().add(settingsBox);
	}

	private final Scene scene = new Scene(root);

	{

		root.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {

			private double offx, offy;
			private boolean drag;

			{
				root.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
					if (!drag)
						return;
					setX(offx + event.getScreenX());
					setY(offy + event.getScreenY());
				});

				root.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> drag = false);
			}

			@Override
			public void handle(MouseEvent event) {
				if (!(event.getPickResult().getIntersectedNode() == root))
					return;
				offx = getX() - event.getScreenX();
				offy = getY() - event.getScreenY();
				drag = true;
			}
		});

		initStyle(StageStyle.TRANSPARENT);
		root.setMinSize(600, 400);
		root.setBorder(new Border(new BorderStroke(ChatRoom.DEFAULT_WINDOW_BORDER_COLOR, BorderStrokeStyle.SOLID, null,
				new BorderWidths(2))));
		root.setBackground(
				new Background(new BackgroundFill(new Color(ChatRoom.DEFAULT_WINDOW_BACKGROUND_COLOR.getRed(),
						ChatRoom.DEFAULT_WINDOW_BACKGROUND_COLOR.getGreen(),
						ChatRoom.DEFAULT_WINDOW_BACKGROUND_COLOR.getBlue(), 1), null, null)));
		setAlwaysOnTop(true);
		setScene(scene);

		scene.setFill(Color.TRANSPARENT);

		// Save & Cancel buttons
		HBox buttonWrapper = new HBox(15, save, cancel);
		buttonWrapper.setAlignment(Pos.CENTER);
		AnchorPane.setBottomAnchor(buttonWrapper, 15d);
		AnchorPane.setLeftAnchor(buttonWrapper, 0d);
		AnchorPane.setRightAnchor(buttonWrapper, 0d);
		root.getChildren().add(buttonWrapper);

		// Settings box
		AnchorPane.setTopAnchor(settingsBox, 15d);
		AnchorPane.setBottomAnchor(settingsBox, 40d);
		AnchorPane.setLeftAnchor(settingsBox, 5d);
		AnchorPane.setRightAnchor(settingsBox, 5d);
		settingsBox.setAlignment(Pos.TOP_CENTER);

		// On focus
		focusedProperty().addListener(
				(ChangeListener<Boolean>) (observable, oldValue, newValue) -> root.setOpacity(newValue ? 1 : 0.2));

		// Login Nodes
		Text accountCategory = new Text("Account");
		accountCategory.setFont(Font.font(Font.getDefault().getSize() + 14));

		Text usernameInfo = new Text("Username:"), passwordInfo = new Text("Password:");
		TextField usernameInput = new TextField(), passwordInput = new PasswordField();
		usernameInput.setPromptText("Username");
		passwordInput.setPromptText("Passwrd123");

		// Login Wrappers
		HBox usernameBox = new HBox(15, usernameInfo, usernameInput);
		HBox passwordBox = new HBox(15, passwordInfo, passwordInput);

		usernameBox.setAlignment(Pos.CENTER);
		passwordBox.setAlignment(Pos.CENTER);

		Button login = new Button("Login");

		VBox loginWrapper = new VBox(10, accountCategory, usernameBox, passwordBox, login);
		loginWrapper.setAlignment(Pos.CENTER);
		loginWrapper.setBorder(new Border(new BorderStroke(ChatRoom.DEFAULT_WINDOW_BORDER_COLOR,
				BorderStrokeStyle.SOLID, null, new BorderWidths(2))));
		loginWrapper.setPadding(new Insets(10));
		loginWrapper.setFillWidth(false);

		settingsBox.getChildren().addAll(loginWrapper);

		// Login impl
		login.setOnAction(event -> {
			try {
				handleLogin(usernameInput.getText(), passwordInput.getText());
			} catch (IOException e) {
				// TODO Notify user
			}
		});

		// Handle Ctrl+Alt+Shift+D; this will open the advanced settings window.
		root.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (!(event.getCode() == KeyCode.D && event.isAltDown() && event.isShiftDown()
						&& event.isControlDown()))
					return;
				event.consume();

				if (advancedSettings.get().isShowing())
					advancedSettings.get().close();
				else
					advancedSettings.get().show();

			}
		});

		root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ESCAPE) {
				close();
				event.consume();
			}
		});

	}

	public _SettingsWindowImpl() {
	}

	public abstract boolean handleLogin(String username, String password) throws IOException;

}
