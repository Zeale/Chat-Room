package org.alixia.chatroom.impl.guis.settings;

import java.io.IOException;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.fxtools.FXTools;
import org.alixia.chatroom.api.internet.Authentication;
import org.alixia.chatroom.api.internet.authmethods.AppAuthMethodImpl;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class _AdvancedSettingsImpl {
	private static final Color DEFAULT_BACKGROUND_COLOR = new Color(ChatRoom.DEFAULT_WINDOW_BACKGROUND_COLOR.getRed(),
			ChatRoom.DEFAULT_WINDOW_BACKGROUND_COLOR.getGreen(), ChatRoom.DEFAULT_WINDOW_BACKGROUND_COLOR.getBlue(), 1);
	private final Stage stage = new Stage(StageStyle.TRANSPARENT);
	private final VBox settingsBox = new VBox(80);
	private final AnchorPane root = new AnchorPane(settingsBox);

	private final Scene scene = new Scene(root);
	private final HBox doneBox = new HBox(120);
	private final HBox authInputBox = new HBox(10);
	private final TextField authOverrideInput = new TextField(), authOverridePort = new TextField();
	private final Button hostAuthServer = new Button("Host Server");
	private final Button apply = new Button("Apply Changes");

	// Construct advanced settings window
	{

		stage.setScene(scene);
		stage.setAlwaysOnTop(true);

		scene.setFill(Color.TRANSPARENT);
		root.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));

		settingsBox.setBackground(new Background(new BackgroundFill(DEFAULT_BACKGROUND_COLOR, null, null)));
		settingsBox.setFillWidth(false);

		final DropShadow shadow = new DropShadow();
		shadow.setRadius(50);
		shadow.setSpread(0);

		settingsBox.setEffect(shadow);

		root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ESCAPE) {
				stage.close();
				event.consume();
			}
		});

		// The advanced settings window is much bigger than the settings window; the
		// main box's padding will be quite large:
		settingsBox.setPadding(new Insets(50));
		AnchorPane.setTopAnchor(settingsBox, 80d);
		AnchorPane.setLeftAnchor(settingsBox, 120d);
		AnchorPane.setBottomAnchor(settingsBox, 80d);
		AnchorPane.setRightAnchor(settingsBox, 120d);

		authOverrideInput.setPromptText(Math.random() < 0.5 ? "dusttoash.org" : "175.26.84.326");// Random IP.
		// Made it up on the spot. I don't even know if it's valid.
		//
		authOverridePort.setPromptText("" + Authentication.DEFAULT_AUTHENTICATION_PORT);

		authInputBox.getChildren().addAll(new Text("Auth Server Override:"), authOverrideInput, authOverridePort);

		hostAuthServer.setPrefWidth(100);
		apply.setPrefWidth(100);
		doneBox.getChildren().addAll(hostAuthServer, apply);

		apply.setOnAction(event -> {
			try {
				authOverridePort.setStyle("");
				Authentication.setDefaultAuthenticationMethod(new AppAuthMethodImpl(authOverrideInput.getText(),
						Integer.parseInt(authOverridePort.getText())));
				stage.close();
			} catch (final NumberFormatException e1) {
				authOverridePort.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
			} catch (final Exception e2) {
				e2.printStackTrace();
			}
		});

		// For now, this will simply start the auth server on the default port...
		hostAuthServer.setOnAction(event -> {
			try {
				Authentication.startAuthServer(Authentication.DEFAULT_AUTHENTICATION_PORT);
				stage.close();
			} catch (final IOException e) {
				e.printStackTrace();
				FXTools.spawnLabelAtMousePos("Failed to start server.", ChatRoom.ERROR_COLOR, stage);
			}
		});

		settingsBox.getChildren().addAll(authInputBox, doneBox);
		settingsBox.setAlignment(Pos.CENTER);

	}

	public _AdvancedSettingsImpl() {
	}

	public void close() {
		stage.close();
	}

	public void hide() {
		stage.hide();
	}

	public final boolean isShowing() {
		return stage.isShowing();
	}

	public final void show() {
		stage.show();
	}

}
