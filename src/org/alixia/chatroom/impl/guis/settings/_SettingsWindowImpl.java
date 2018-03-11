package org.alixia.chatroom.impl.guis.settings;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.items.LateLoadItem;
import org.alixia.chatroom.guis.ChatRoomWindow;
import org.alixia.chatroom.resources.fxnodes.popbutton.PopButton;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;

abstract class _SettingsWindowImpl extends ChatRoomWindow {

	private final Button close = new PopButton("close");

	private final VBox settingsBox = new VBox();
	private final ScrollPane scrollWrapper = new ScrollPane(settingsBox);
	private final LateLoadItem<_AdvancedSettingsImpl> advancedSettings = new LateLoadItem<>(
			() -> new _AdvancedSettingsImpl());

	{
		contentPane.getChildren().add(scrollWrapper);

		// Styling scrollWrapper
		scene.getStylesheets().add("org/alixia/chatroom/stylesheet.css");
		scrollWrapper.setBackground(null);
		scrollWrapper.setFitToHeight(true);
		scrollWrapper.setFitToWidth(true);

		initStyle(StageStyle.TRANSPARENT);
		setAlwaysOnTop(true);
		setScene(scene);

		// Save & Cancel buttons

		close.setOnAction(event -> close());

		final HBox buttonWrapper = new HBox(15, close);
		buttonWrapper.setAlignment(Pos.CENTER);
		AnchorPane.setBottomAnchor(buttonWrapper, 15d);
		AnchorPane.setLeftAnchor(buttonWrapper, 0d);
		AnchorPane.setRightAnchor(buttonWrapper, 0d);
		contentPane.getChildren().add(buttonWrapper);

		// Settings box
		AnchorPane.setTopAnchor(scrollWrapper, 15d);
		AnchorPane.setBottomAnchor(scrollWrapper, 40d);
		AnchorPane.setLeftAnchor(scrollWrapper, 5d);
		AnchorPane.setRightAnchor(scrollWrapper, 5d);
		settingsBox.setAlignment(Pos.TOP_CENTER);

		// On focus
		focusedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> contentPane
				.setOpacity(newValue ? 1 : 0.2));

		// Login Nodes
		final Text accountCategory = new Text("Account");
		accountCategory.setFont(Font.font(Font.getDefault().getSize() + 14));

		final Text usernameInfo = new Text("Username:"), passwordInfo = new Text("Password:");
		final TextField usernameInput = new TextField(), passwordInput = new PasswordField();
		usernameInput.setPromptText("Username");
		passwordInput.setPromptText("Passwrd123");

		// Login Wrappers
		final HBox usernameBox = new HBox(15, usernameInfo, usernameInput);
		final HBox passwordBox = new HBox(15, passwordInfo, passwordInput);

		usernameBox.setAlignment(Pos.CENTER);
		passwordBox.setAlignment(Pos.CENTER);

		final Button login = new Button("Login");

		final VBox loginWrapper = new VBox(10, accountCategory, usernameBox, passwordBox, login);
		loginWrapper.setAlignment(Pos.CENTER);
		loginWrapper.setBorder(new Border(new BorderStroke(ChatRoom.DEFAULT_WINDOW_BORDER_COLOR,
				BorderStrokeStyle.SOLID, null, new BorderWidths(2))));
		loginWrapper.setPadding(new Insets(10));
		loginWrapper.setFillWidth(false);

		settingsBox.getChildren().addAll(loginWrapper);

		// Login impl
		login.setOnAction(event -> handleLogin(usernameInput.getText(), passwordInput.getText()));

		// Handle Ctrl+Alt+Shift+D; this will open the advanced settings window.
		contentPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (!(event.getCode() == KeyCode.D && event.isAltDown() && event.isShiftDown() && event.isControlDown()))
				return;
			event.consume();

			if (advancedSettings.get().isShowing())
				advancedSettings.get().close();
			else
				advancedSettings.get().show();

		});

		contentPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ESCAPE) {
				close();
				event.consume();
			}
		});

		setMenubarType(false);

	}

	public _SettingsWindowImpl() {
	}

	public abstract void handleLogin(String username, String password);

}
