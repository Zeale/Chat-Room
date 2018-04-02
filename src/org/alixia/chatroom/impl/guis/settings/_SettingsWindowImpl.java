package org.alixia.chatroom.impl.guis.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.data.JarData;
import org.alixia.chatroom.api.fx.tools.FXTools;
import org.alixia.chatroom.api.guis.ChatRoomWindow;
import org.alixia.chatroom.api.items.LateLoadItem;
import org.alixia.chatroom.impl.data.HomeDir;
import org.alixia.chatroom.resources.fxnodes.popbutton.PopButton;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.StageStyle;

abstract class _SettingsWindowImpl extends ChatRoomWindow {

	private final Button close = new PopButton("close");

	private final VBox settingsBox = new VBox(7);
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
		focusedProperty().addListener(
				(ChangeListener<Boolean>) (observable, oldValue, newValue) -> root.setOpacity(newValue ? 1 : 0.2));

		/*
		 * Account Box
		 */

		{
			final Text usernameInfo = new Text("Username:"), passwordInfo = new Text("Password:");
			final TextField usernameInput = new TextField(), passwordInput = new PasswordField();
			usernameInput.setPromptText("Username");
			passwordInput.setPromptText("Passwrd123");

			final HBox usernameBox = new HBox(15, usernameInfo, usernameInput);
			final HBox passwordBox = new HBox(15, passwordInfo, passwordInput);

			usernameBox.setAlignment(Pos.CENTER);
			passwordBox.setAlignment(Pos.CENTER);

			final Button login = new Button("Login");

			addWrapper("Account", 10, usernameBox, passwordBox, login);

			// Login impl
			login.setOnAction(event -> handleLogin(usernameInput.getText(), passwordInput.getText()));
		}

		/*
		 * Installation Box
		 */

		{
			Text warning = new Text(
					"This will attempt to restart the program. If restarting it fails, the program will simply close.");
			warning.setFill(Color.CRIMSON);

			final Button installDirSelectorButton = new Button("Select Folder");
			installDirSelectorButton.setTextFill(Color.BLUE);
			final TextField installDirInput = new TextField();

			installDirInput.setOnKeyPressed(event -> installDirInput.setBorder(null));

			final HBox installDirWrapper = new HBox(15, installDirInput, installDirSelectorButton);

			final Button installButton = new Button("Install");

			addWrapper("Installation", 10, warning, installDirWrapper, installButton);

			// File selector impl

			final DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Install Location");
			chooser.setInitialDirectory(
					HomeDir.isHomeDirSet() ? HomeDir.getHomeDir() : JarData.getRuntimeLocation().getParentFile());

			installDirSelectorButton.setOnAction(event -> {
				final File result = chooser.showDialog(_SettingsWindowImpl.this);
				if (result == null)
					return;
				installDirInput.setText(result.getAbsolutePath());
			});

			installButton.setOnAction(new EventHandler<ActionEvent>() {

				private final BorderWidths errorBorderWidth = new BorderWidths(2.5);

				@Override
				public void handle(final ActionEvent event) {

					final String location = installDirInput.getText();
					if (location.isEmpty()) {
						installDirInput.setBorder(new Border(
								new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, null, errorBorderWidth)));
						return;
					}

					final File saveLocation = new File(location);

					try {
						HomeDir.setSaveLocation(saveLocation);

						// Attempt to restart.
						if (!ChatRoom.isDevelopmentEnvironment()) {
							Process proc = Runtime.getRuntime().exec(
									new String[] { "java", "-jar", JarData.getRuntimeLocation().getAbsolutePath() });
							Reader reader = new InputStreamReader(proc.getInputStream());
							// Skip over all the input.
							while (reader.ready())
								reader.read();

							// Close this instance of the program.
							Platform.exit();
						}
					} catch (final FileNotFoundException e) {
						installDirInput.setBorder(new Border(
								new BorderStroke(Color.GOLD, BorderStrokeStyle.SOLID, null, errorBorderWidth)));
						return;
					} catch (final NullPointerException e) {
						// This should never be thrown.
						e.printStackTrace();
					} catch (final RuntimeException e) {
						FXTools.spawnLabelAtMousePos("Folders could not be created at the location specified...",
								ChatRoom.ERROR_COLOR, _SettingsWindowImpl.this);
					} catch (final IOException e) {
						FXTools.spawnLabelAtMousePos("An error occurred while reading or writing the program.",
								ChatRoom.ERROR_COLOR, _SettingsWindowImpl.this);
						e.printStackTrace();
					}
				}

			});

		}

		// Get whether or not the program is installed

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

	protected VBox addWrapper(final String title, final int spacing, final Node... children) {
		// Make box
		final VBox box = new VBox(spacing, children);
		settingsBox.getChildren().add(box);

		// Add title
		final Text titleNode = new Text(title);
		titleNode.setFont(Font.font(Font.getDefault().getSize() + 14));
		box.getChildren().add(0, titleNode);

		// Style box
		box.setAlignment(Pos.CENTER);
		box.setBorder(new Border(new BorderStroke(ChatRoom.DEFAULT_WINDOW_BORDER_COLOR, BorderStrokeStyle.SOLID, null,
				new BorderWidths(2))));
		box.setPadding(new Insets(10));
		box.setFillWidth(false);

		// Return box
		return box;
	}

	protected abstract void handleLogin(String username, String password);

	// protected abstract void handleInstall();

}
