package org.alixia.chatroom;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.alixia.chatroom.api.Account;
import org.alixia.chatroom.api.Console;
import org.alixia.chatroom.api.OS;
import org.alixia.chatroom.api.Printable;
import org.alixia.chatroom.api.commands.CommandManager;
import org.alixia.chatroom.api.connections.Client;
import org.alixia.chatroom.api.connections.ConnectionListener;
import org.alixia.chatroom.api.connections.Server;
import org.alixia.chatroom.api.connections.messages.client.BasicUserMessage;
import org.alixia.chatroom.api.connections.messages.client.UserMessage;
import org.alixia.chatroom.api.connections.messages.client.requests.LogoutRequest;
import org.alixia.chatroom.api.connections.messages.client.requests.NameChangeRequest;
import org.alixia.chatroom.api.connections.messages.server.ServerMessage;
import org.alixia.chatroom.api.connections.voicecall.CallClient;
import org.alixia.chatroom.api.connections.voicecall.CallServer;
import org.alixia.chatroom.api.data.JarData;
import org.alixia.chatroom.api.history.HistoryManager;
import org.alixia.chatroom.api.internet.Authentication;
import org.alixia.chatroom.api.items.LateLoadItem;
import org.alixia.chatroom.api.logging.Logger;
import org.alixia.chatroom.api.texts.BasicInfoText;
import org.alixia.chatroom.api.texts.BoldText;
import org.alixia.chatroom.api.texts.Println;
import org.alixia.chatroom.impl.data.DirectoryCreationFailedException;
import org.alixia.chatroom.impl.data.HomeDir;
import org.alixia.chatroom.impl.data.LocalInstallDirectoryBuggedException;
import org.alixia.chatroom.impl.guis.chatroom.ChatRoomGUI;
import org.alixia.chatroom.impl.guis.settings.SettingsGUI;

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

	public static File getHomeDirectory() {
		return HomeDir.getHomeDir();
	}

	public static boolean isDevelopmentEnvironment() {
		return !new File(JarData.class.getProtectionDomain().getCodeSource().getLocation().getFile()).isFile();
	}

	private Account account;
	private CallServer callServer;

	private CallClient callClient;

	private ChatRoomGUI gui;

	public final Printable printer = new Printable() {

		@Override
		public void print(final String text, final Color color) {
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
			println("You disconnected from the server.", INFO_COLOR);
			client = null;
		}

		@Override
		public void objectReceived(final Serializable object) {
			if (object instanceof UserMessage)
				Platform.runLater(() -> ((UserMessage) object).toConsoleText().print(console));
			else if (object instanceof ServerMessage)
				Platform.runLater(() -> ((ServerMessage) object).toConsoleText().print(console));

		}
	};

	public final CommandManager commandManager = new CommandManager();
	private Client client;

	private Server server;
	public final LateLoadItem<SettingsGUI> settingsInstance = new LateLoadItem<>(SettingsGUI::new);

	private String username = null;

	private final HistoryManager manager = new HistoryManager();

	ChatRoom() {
	}

	public void createNewClient(final String host) throws UnknownHostException, IOException {
		createNewClient(host, DEFAULT_CHAT_PORT);
	}

	public boolean createNewClient(final String host, final int port)
			throws UnknownHostException, IOException, IllegalArgumentException {
		final Client client = new Client(host, port);

		client.setListener(clientListener);

		if (isLoggedIn())
			client.sendObject(ChatRoom.INSTANCE.getAccount());
		if (username != null)
			client.sendObject(new NameChangeRequest(username));
		this.client = client;
		return true;
	}

	public void executeCommand(final String command) {
		commandManager.runCommand(command);
	}

	public Account getAccount() {
		return account;
	}

	public CallClient getCallClient() {
		return callClient;
	}

	public CallServer getCallServer() {
		return callServer;
	}

	public Client getClient() {
		return client;
	}

	public ChatRoomGUI getGUI() {
		return gui;
	}

	public Server getServer() {
		return server;
	}

	public String getUsername() {
		return username == null ? "Anonymous" : username;
	}

	/**
	 * Returns whether or not there is a connection to a server. If there is an
	 * active client, this method returns <code>true</code>. Otherwise, it returns
	 * false.
	 *
	 * @return <code>{@link #getClient()}!=null</code>
	 */
	public boolean isClientOpen() {
		return getClient() != null;
	}

	public boolean isLoggedIn() {
		return account != null;
	}

	public boolean isServerOpen() {
		return server != null;
	}

	public boolean isUsernameSet() {
		return username != null;
	}

	public void login(final Account account) {
		this.account = account;
		if (isClientOpen())
			getClient().sendObject(ChatRoom.INSTANCE.getAccount());
	}

	public void logout() {
		account = null;
		if (isClientOpen())
			getClient().sendObject(new LogoutRequest());
	}

	/**
	 * Called when user pushes send.
	 */
	private void onUserSubmit() {
		final String text = getGUI().input.getText();

		manager.send(text);

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

	public void print(final String text, final Color color) {
		new BasicInfoText(text, color).print(console);
	}

	public void println() {
		new Println(console);
	}

	public void println(final String text, final Color color) {
		print(text, color);
		println();
	}

	/**
	 * Sends text as the user.
	 *
	 * @param text
	 *            The text.
	 */
	public void sendText(final String text) {

		if (isClientOpen())
			getClient().sendObject(new BasicUserMessage(text));
		else {
			print("You can only send messages to a server through a client. Do ", ERROR_COLOR);
			print("/new help ", Color.ORANGERED);
			println("For help with connections.", ERROR_COLOR);
		}

	}

	public void setCallClient(final CallClient callClient) {
		this.callClient = callClient;
	}

	public void setCallServer(final CallServer callServer) {
		this.callServer = callServer;
	}

	void setStage(final Stage stage) {
		if (getGUI() != null)
			throw new RuntimeException("Already initialized");

		gui = new ChatRoomGUI(stage);

		try {
			tryInit();
		} catch (final Exception e) {
			final Text error = new Text("An error occurred.");
			error.setFont(Font.font(30));
			error.setFill(Color.CRIMSON);
			getGUI().flow.setTextAlignment(TextAlignment.CENTER);
			getGUI().flow.getChildren().add(error);

		}

	}

	public void setUsername(final String username) {
		this.username = username;
		if (isClientOpen())
			getClient().sendObject(new NameChangeRequest(username));
	}

	public void startServer(final int port) throws RuntimeException, IOException {
		if (isServerOpen())
			throw new RuntimeException("Server already running.");
		server = new Server(port);
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
			if (isServerOpen())
				try {
					getServer().stop();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			if (isClientOpen())
				getClient().closeConnection();
		});

		getGUI().input.setOnKeyPressed(event -> {
			if (event.getCode().equals(KeyCode.ENTER) && !(event.isShiftDown() || event.isControlDown())) {
				onUserSubmit();
				event.consume();
			} else if (event.getCode() == KeyCode.UP && event.isControlDown())
				getGUI().input.setText(manager.getOlder());
			else if (event.getCode() == KeyCode.DOWN && event.isControlDown())
				getGUI().input.setText(manager.getNewer());
		});
		getGUI().sendButton.setOnAction(event -> {
			onUserSubmit();
			event.consume();
		});

		println("Checking to see if you've set an installation directory before...", INFO_COLOR);
		if (HomeDir.hasLocalHomeDirectory()) {
			println("The directory has been set. Attempting to read it.", SUCCESS_COLOR);

			try {
				HomeDir.loadLocalHomeDirectory();
			} catch (final LocalInstallDirectoryBuggedException e1) {
				println("The program has dectected that you've set an installation directory, but the location of the directory is bugged (i.e. it couldn't be read from a file).",
						ERROR_COLOR);
				print("The program will function as though you never set it to install for now. You can set a new installation directory (or set the same one as before) in ",
						ERROR_COLOR);
				print("/settings", INFO_COLOR);
				println(".", ERROR_COLOR);
				e1.printStackTrace();
			} catch (final DirectoryCreationFailedException e1) {
				println("Found an installation location but failed to create the necessary directories.", ERROR_COLOR);
				println("Here is the directory that couldn't be created:", ERROR_COLOR);
				println(e1.directory.getAbsolutePath(), Color.ORANGERED);
				e1.printStackTrace();
			}

		} else {
			print("You haven't set an installation directory yet. See ", INFO_COLOR);
			print("/settings ", Color.GOLDENROD);
			println("to set one (if you want).", INFO_COLOR);
		}

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
				catch (final MalformedURLException e1) {
					println("There was an error parsing the file's web address.", ERROR_COLOR);
				} catch (final IOException e2) {
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

			} catch (final MalformedURLException e3) {
				println("There was an error while trying to locate the file.", ERROR_COLOR);
			} catch (final IOException e4) {
				println("There was an error while trying to download the file.", ERROR_COLOR);
			} catch (final URISyntaxException e5) {
				println("There was an error parsing the file's web address.", ERROR_COLOR);
			} catch (final UnsupportedOperationException e6) {
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
