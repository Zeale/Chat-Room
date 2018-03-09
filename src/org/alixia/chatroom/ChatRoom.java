package org.alixia.chatroom;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.alixia.chatroom.api.Console;
import org.alixia.chatroom.api.OS;
import org.alixia.chatroom.api.Printable;
import org.alixia.chatroom.api.UserData;
import org.alixia.chatroom.api.items.LateLoadItem;
import org.alixia.chatroom.commands.CommandManager;
import org.alixia.chatroom.connections.ClientManager;
import org.alixia.chatroom.connections.ConnectionListener;
import org.alixia.chatroom.connections.ServerManager;
import org.alixia.chatroom.connections.messages.client.BasicUserMessage;
import org.alixia.chatroom.connections.messages.client.UserMessage;
import org.alixia.chatroom.connections.voicecall.CallClient;
import org.alixia.chatroom.connections.voicecall.CallServer;
import org.alixia.chatroom.impl.guis.settings.ChatRoomGUI;
import org.alixia.chatroom.impl.guis.settings.Settings;
import org.alixia.chatroom.internet.Authentication;
import org.alixia.chatroom.logging.Logger;
import org.alixia.chatroom.texts.BasicInfoText;
import org.alixia.chatroom.texts.BasicUserText;
import org.alixia.chatroom.texts.BoldText;
import org.alixia.chatroom.texts.Println;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 
 * @author Zeale
 *
 */
public class ChatRoom {

	public static final int DEFAULT_CALL_SAMPLE_RATE = 96000;
	public static final int DEFAULT_CALL_PORT = 25369;
	public static final int DEFAULT_AUTHENTICATION_PORT = Authentication.DEFAULT_AUTHENTICATION_PORT;
	public static final String DEFAULT_AUTHENTICATION_SERVER = Authentication.DEFAULT_AUTHENTICATION_SERVER;
	public static final int DEFAULT_CHAT_PORT = 25000;
	public static final Color DEFAULT_WINDOW_BACKGROUND_COLOR = ChatRoomGUI.DEFAULT_WINDOW_BACKGROUND_COLOR,
			DEFAULT_WINDOW_BORDER_COLOR = ChatRoomGUI.DEFAULT_WINDOW_BORDER_COLOR;
	public static final Color ERROR_COLOR = Color.RED, INFO_COLOR = Color.LIGHTBLUE, SUCCESS_COLOR = Color.GREEN,
			WARNING_COLOR = Color.GOLD;

	public static final ChatRoom INSTANCE = new ChatRoom();

	public static final Logger LOGGER = Logger.CHAT_ROOM_LOGGER;

	public UserData userData;
	private CallServer callServer;
	private CallClient callClient;
	private String username = "Unnamed";

	private ChatRoomGUI gui;

	public final Printable printer = new Printable() {

		@Override
		public void print(String text, Color color) {
			ChatRoom.this.print(text, color);
		}

		@Override
		public void println() {
			// ChatRoom's impl of println is different from the default impl by the
			// interface.
			ChatRoom.this.println();
		}
	};

	public final Console console = text -> Platform.runLater(() -> getGUI().flow.getChildren().add(text));

	private final ConnectionListener clientListener = new ConnectionListener() {

		@Override
		public void connectionClosed() {
			clients.unselectItem();
		}

		@Override
		public void objectReceived(Serializable object) {
			if (object instanceof UserMessage)
				Platform.runLater(() -> ((UserMessage) object).toConsoleText().print(console));

		}
	};

	public final CommandManager commandManager = new CommandManager();

	public final ClientManager clients = new ClientManager(clientListener);
	public final ServerManager servers = new ServerManager();
	public final LateLoadItem<Settings> settingsInstance = new LateLoadItem<>(() -> new Settings());

	ChatRoom() {
	}

	public void executeCommand(String command) {
		commandManager.runCommand(command);
	}

	public CallClient getCallClient() {
		return callClient;
	}

	public CallServer getCallServer() {
		return callServer;
	}

	public ChatRoomGUI getGUI() {
		return gui;
	}

	public String getUsername() {
		return username;
	}

	public boolean isLoggedIn() {
		return userData != null;
	}

	/**
	 * Called when user pushes send.
	 */
	private void onUserSubmit() {
		String text = getGUI().input.getText();

		if (!commandManager.runCommand(text))
			if (text.isEmpty())
				return;
			else if (text.startsWith(commandManager.getCommandChar()))
				println("That command was not recognized.", Color.AQUA);
			else
				sendText(text);

		getGUI().input.setText("");
	}

	public void openSettingsWindow() {
		settingsInstance.get().show();
	}

	public void print(String text, Color color) {
		new BasicInfoText(text, color).print(console);
	}

	public void println() {
		new Println(console);
	}

	public void println(String text, Color color) {
		print(text, color);
		println();
	}

	/**
	 * Sends text as the user.
	 * 
	 * @param text
	 *            The text.
	 */
	public void sendText(String text) {

		if (clients.isItemSelected()) {
			clients.getSelectedItem().sendObject(new BasicUserMessage(username, text));
			new BasicUserText(username, text).print(console);
		} else {
			print("You can only send messages to a server through a client. Do ", ERROR_COLOR);
			print("/new help ", Color.ORANGERED);
			println("For help with connections.", ERROR_COLOR);
		}

	}

	public void setCallClient(CallClient callClient) {
		this.callClient = callClient;
	}

	public void setCallServer(CallServer callServer) {
		this.callServer = callServer;
	}

	void setStage(Stage stage) {
		if (getGUI() != null)
			throw new RuntimeException("Already initialized");

		gui = new ChatRoomGUI(stage);

		try {
			tryInit();
		} catch (Exception e) {
			Text error = new Text("An error occurred.");
			error.setFont(Font.font(30));
			error.setFill(Color.CRIMSON);
			getGUI().flow.setTextAlignment(TextAlignment.CENTER);
			getGUI().flow.getChildren().add(error);

		}

	}

	public void setUsername(String username) {
		this.username = username;
	}

	private void tryInit() {

		getGUI().stage.initStyle(StageStyle.DECORATED);
		getGUI().addBorder();
		getGUI().root.setCenter(getGUI().contentWrapper);
		getGUI().stage.show();

		getGUI().scene.getStylesheets().add("org/alixia/chatroom/stylesheet.css");
		getGUI().flowWrapper.setBackground(null);
		getGUI().flowWrapper.setFitToWidth(true);
		getGUI().flow.setMinHeight(200);

		getGUI().stage.setMinHeight(400);
		getGUI().stage.setMinWidth(600);

		getGUI().stage.setOnCloseRequest(event -> {
			servers.close();
			clients.close();
		});

		getGUI().input.setOnKeyPressed(event -> {
			if (event.getCode().equals(KeyCode.ENTER)) {
				onUserSubmit();
				event.consume();
			}
		});
		getGUI().sendButton.setOnAction(event -> {
			onUserSubmit();
			event.consume();
		});

		print("Startup took ", SUCCESS_COLOR);
		new BoldText("" + (System.currentTimeMillis() - Launch.STARTUP_TIME) + " ", Color.FIREBRICK).print(console);
		println("milliseconds!", SUCCESS_COLOR);

		print("Connect to a server with ", Color.RED);
		print("/new client (hostname) [port] (client-name) ", Color.GREEN);
		print("to get started!", Color.RED);
		println();
		print("To start hosting a server, do ", Color.RED);
		print("/new server [port] (server-name)", Color.GREEN);
		println(".", Color.RED);
		print("Do ", Color.PURPLE);
		print("/help ", Color.WHITE);
		println("for more help.", Color.PURPLE);

	}

	public void updateProgram() {

		TRY_DOWNLOAD: {
			// Windows
			if (OS.getOS() == OS.WINDOWS)
				try (InputStream is = new URL("http://dusttoash.org/chat-room/ChatRoom.jar").openStream()) {
					Files.copy(is, new File(System.getProperty("user.home") + "\\Desktop\\ChatRoom.jar").toPath(),
							StandardCopyOption.REPLACE_EXISTING);
					// Success
					println("The newest version of Chat Room was placed on your desktop.", SUCCESS_COLOR);
					break TRY_DOWNLOAD;
				}
				// If there is a failure, we won't get to the "break TRY_DOWNLOAD"
				// statement, so the below try block will be run, and Chat Room will
				// attempt to open the latest version in the default browser.
				catch (MalformedURLException e1) {
					println("There was an error parsing the file's web address.", ERROR_COLOR);
				} catch (IOException e2) {
					print("There was an error while trying to retrieve the file from the address: ", ERROR_COLOR);
					println("http://dusttoash.org/chat-room/ChatRoom.jar", Color.WHITE);
					println("Attempting to open the file in your browser...", Color.ORANGE);
					println();
				}

			// Either the OS is not Windows, (and thus I don't know if their Desktop's
			// location is their homedir +"\Desktop"), or the attempt to download the
			// file failed.
			try {
				// This may throw an exception skipping the break and going to the catch
				// blocks. right after that, we exit the try and go over the print
				// statements for failures then we return.
				Desktop.getDesktop().browse(new URL("http://dusttoash.org/chat-room/ChatRoom.jar").toURI());

				break TRY_DOWNLOAD;// And continue on to print our success.

			} catch (MalformedURLException e3) {
				println("There was an error while trying to locate the file.", ERROR_COLOR);
			} catch (IOException e4) {
				println("There was an error while trying to download the file.", ERROR_COLOR);
			} catch (URISyntaxException e5) {
				println("There was an error parsing the file's web address.", ERROR_COLOR);
			} catch (UnsupportedOperationException e6) {
				print("Apparently, your operating system does not support Chat Room opening a link with your default browser. Here is the link to the file: ",
						ERROR_COLOR);
				println("http://dusttoash.org/chat-room/ChatRoom.jar", Color.WHITE);
			}

			println();
			println();
			println("The latest version could not be downloaded...", ERROR_COLOR);

		}
		println("Opening the file in your browser seems to have succeeded. Please copy the file to wherever and run it for the latest version.",
				Color.WHITE);
		println("You can close the program and discard this file, then open the new one with the new updates.",
				Color.WHITE);

	}

}
