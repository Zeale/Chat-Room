package org.alixia.chatroom;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.alixia.chatroom.commands.Command;
import org.alixia.chatroom.commands.CommandManager;
import org.alixia.chatroom.connections.Client;
import org.alixia.chatroom.connections.ClientManager;
import org.alixia.chatroom.connections.ConnectionListener;
import org.alixia.chatroom.connections.Server;
import org.alixia.chatroom.connections.ServerManager;
import org.alixia.chatroom.connections.messages.client.BasicUserMessage;
import org.alixia.chatroom.connections.messages.client.UserMessage;
import org.alixia.chatroom.resources.fxnodes.popbutton.PopButton;
import org.alixia.chatroom.texts.BasicInfoText;
import org.alixia.chatroom.texts.BasicUserText;
import org.alixia.chatroom.texts.ConsoleText;
import org.alixia.chatroom.texts.Println;
import org.alixia.chatroom.texts.SimpleText;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ChatRoom {

	private static final Color ERROR_COLOR = Color.RED, INFO_COLOR = Color.LIGHTBLUE, SUCCESS_COLOR = Color.GREEN;

	private static final Color WINDOW_BORDER_COLOR = new Color(0.2, 0.2, 0.2, 1),
			NODE_OUTPUT_COLOR = new Color(0, 0, 0, 0.3), NODE_ITEM_COLOR = Color.DARKGRAY,
			WINDOW_BACKGROUND_COLOR = new Color(0.3, 0.3, 0.3, 0.8);

	private static final Background getBackground(Color color) {
		return new Background(new BackgroundFill(color, null, null));
	}

	private static final int DEFAULT_PORT = 25000;

	private String username = "Unnamed";

	// Some of these fields have aliases.
	private final TextFlow flow = new TextFlow();
	private final TextArea input = new TextArea();
	private final Button sendButton = new PopButton("Send");
	private final ScrollPane flowWrapper = new ScrollPane(flow);
	private final AnchorPane contentWrapper = new AnchorPane(flowWrapper, input, sendButton);
	private final BorderPane root = new BorderPane();

	private final Scene scene;
	private final Stage stage;

	private final ConnectionListener clientListener = new ConnectionListener() {

		@Override
		public void objectReceived(Serializable object) {
			if (object instanceof UserMessage)
				Platform.runLater(() -> ((UserMessage) object).toConsoleText().print(flow));

		}

		@Override
		public void connectionClosed() {
			clients.unselectItem();
		}
	};

	private final CommandManager commandManager = new CommandManager();
	private final ClientManager clients = new ClientManager(clientListener);
	private final ServerManager servers = new ServerManager();

	ChatRoom(Stage stage) {
		this.stage = stage;

		stage.setWidth(800);
		stage.setHeight(600);

		flow.setBackground(getBackground(NODE_OUTPUT_COLOR));
		input.setBackground(getBackground(NODE_OUTPUT_COLOR));
		input.setStyle("-fx-text-fill: darkgray; ");

		AnchorPane.setLeftAnchor(flowWrapper, 50d);
		AnchorPane.setRightAnchor(flowWrapper, 50d);
		AnchorPane.setTopAnchor(flowWrapper, 0d);
		AnchorPane.setBottomAnchor(flowWrapper, 250d);

		AnchorPane.setBottomAnchor(input, 0d);
		AnchorPane.setLeftAnchor(input, 0d);
		AnchorPane.setRightAnchor(input, 0d);

		AnchorPane.setRightAnchor(sendButton, 50d);
		AnchorPane.setBottomAnchor(sendButton, 88.5);

		input.setMaxHeight(200);

		// Add a ScrollPane to wrap flow
		contentWrapper.setBackground(getBackground(WINDOW_BACKGROUND_COLOR));

		scene = new Scene(root);

		stage.setScene(scene);

		try {
			tryInit();
		} catch (Exception e) {
			Text error = new Text("An error occurred.");
			error.setFont(Font.font(30));
			error.setFill(Color.CRIMSON);
			flow.setTextAlignment(TextAlignment.CENTER);
			flow.getChildren().add(error);

		}
	}

	private void addBorder() {
		final Color ITEM_COLOR = ChatRoom.NODE_ITEM_COLOR, BACKGROUND_COLOR = WINDOW_BORDER_COLOR;

		StackPane close = new StackPane(), minimize = new StackPane(), expand = new StackPane();

		Border border = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(2)));

		close.setPrefSize(26, 26);
		minimize.setPrefSize(26, 26);
		expand.setPrefSize(26, 26);

		Background background = new Background(new BackgroundFill(BACKGROUND_COLOR, null, null));
		close.setBackground(background);
		minimize.setBackground(background);
		expand.setBackground(background);

		final double size = 28;
		{
			if (OS.getOS() == OS.WINDOWS) {

				// Pos is the rect with a positive slope, neg is the negative sloped rect.
				Shape pos = new Rectangle(24, 2), neg = new Rectangle(24, 2);
				neg.setRotate(45);
				pos.setRotate(-45);
				pos.setFill(ITEM_COLOR);
				neg.setFill(ITEM_COLOR);

				neg.setStroke(ITEM_COLOR);
				neg.setStrokeWidth(1);
				pos.setStroke(ITEM_COLOR);
				pos.setStrokeWidth(1);

				StackPane.setAlignment(neg, Pos.CENTER);
				StackPane.setAlignment(pos, Pos.CENTER);

				close.getChildren().addAll(pos, neg);

				// Expand/Maximize
				Rectangle max = new Rectangle(20, 20);
				max.setFill(Color.TRANSPARENT);
				max.setStroke(ITEM_COLOR);
				max.setStrokeWidth(2.5);

				StackPane.setAlignment(max, Pos.CENTER);

				expand.getChildren().add(max);

				// Minimize
				Rectangle min = new Rectangle(22, 2);
				StackPane.setAlignment(min, Pos.BOTTOM_CENTER);
				min.setFill(ITEM_COLOR);
				min.setStroke(ITEM_COLOR);
				min.setStrokeWidth(1);
				minimize.setPadding(new Insets(0, 0, 2, 0));
				minimize.getChildren().add(min);
			} else {

				Shape closeFill, minimizeFill, expandFill;
				closeFill = new Circle(size / 5);
				minimizeFill = new Circle(size / 5);
				expandFill = new Circle(size / 5);
				closeFill.setFill(Color.CORAL);
				expandFill.setFill(Color.GOLD);
				minimizeFill.setFill(Color.LIMEGREEN);
				close.getChildren().add(closeFill);
				minimize.getChildren().add(minimizeFill);
				expand.getChildren().add(expandFill);

			}

		}

		close.setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.PRIMARY)) {
				stage.close();
				Platform.exit();
			}
		});

		expand.setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.PRIMARY))
				stage.setMaximized(!stage.isMaximized());
		});

		minimize.setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.PRIMARY))
				stage.setIconified(true);
		});

		// Menu bar
		HBox menuBar = new HBox(OS.getOS() == OS.WINDOWS ? minimize : close, expand,
				OS.getOS() == OS.WINDOWS ? close : minimize);

		menuBar.setBorder(new Border(new BorderStroke(null, null, BACKGROUND_COLOR, null, null, null,
				BorderStrokeStyle.SOLID, null, null, new BorderWidths(2), null)));
		menuBar.setBackground(background);
		menuBar.setMaxHeight(30);
		menuBar.setPrefHeight(30);
		menuBar.setMinHeight(30);
		menuBar.setSpacing(2);
		if (OS.getOS() == OS.WINDOWS)
			menuBar.setAlignment(Pos.CENTER_RIGHT);
		else
			menuBar.setAlignment(Pos.CENTER_LEFT);
		// Root
		root.setBorder(border);
		root.setTop(menuBar);
		stage.initStyle(StageStyle.TRANSPARENT);
	}

	private void tryInit() {

		stage.initStyle(StageStyle.DECORATED);
		addBorder();
		root.setCenter(contentWrapper);
		stage.show();

		scene.getStylesheets().add("org/alixia/chatroom/stylesheet.css");
		flowWrapper.setBackground(null);
		flowWrapper.setFitToWidth(true);
		flow.setMinHeight(200);

		stage.setOnCloseRequest(event -> {
			servers.close();
			clients.close();
		});

		input.setOnKeyPressed(event -> {
			if (event.getCode().equals(KeyCode.ENTER)) {
				onUserSubmit();
				event.consume();
			}
		});
		sendButton.setOnAction(event -> {
			onUserSubmit();
			event.consume();
		});

		println("Setting up commands...", Color.BISQUE);
		{

			commandManager.addCommand(new Command() {

				@Override
				protected boolean match(String name) {
					return name.equalsIgnoreCase("update");
				}

				@Override
				protected void act(String name, String... args) {
					// Update the program

					if (args.length > 0) {
						if (args[0].equalsIgnoreCase("force") || args[0].equals("-f")) {
							println("Forcefully updating the program.", ERROR_COLOR);
							println("This ignores checks to see whether or not your version is the latest. If the update command is causing problems with version checking, this command is useful.",
									Color.CYAN);
							updateProgram();
						}
					} else {
						// versions
						int latest = 0, current = 0;
						boolean currSuccess = false, lateSuccess = false;

						println();
						println();
						println("Attempting to connect to the download site.", INFO_COLOR);

						try {
							Reader versionInput = new InputStreamReader(
									new URL("http://dusttoash.org/chat-room/version").openStream());
							int n;
							int inc = 0;
							while ((n = versionInput.read()) != -1)
								if (Character.isDigit(n))
									latest += Math.pow(10, inc++) * Integer.parseInt("" + (char) n);

							lateSuccess = true;

							print("The latest available version is ", SUCCESS_COLOR);
							print("" + latest, Color.WHITE);
							println(".", SUCCESS_COLOR);
							println();

						} catch (IOException e) {
							println("An error occurred while trying to connect to the download server. The latest version could not be determined.",
									ERROR_COLOR);
						}

						println("Attempting to determine the version that you have.", INFO_COLOR);
						try {
							Reader versionInput = new InputStreamReader(getClass().getResourceAsStream("/version"));
							int n;
							int inc = 0;
							while ((n = versionInput.read()) != -1)
								if (Character.isDigit(n))
									current += Math.pow(10, inc++) * Integer.parseInt("" + (char) n);

							currSuccess = true;

							print("You have version ", SUCCESS_COLOR);
							print("" + current, Color.WHITE);
							println(".", SUCCESS_COLOR);

						} catch (NullPointerException e) {
							println("The version of your copy of this application could not be determined.",
									ERROR_COLOR);
						} catch (IOException e) {
							println("There was an error while reading some data inside the app. Your local version could not be determined.",
									ERROR_COLOR);
						}

						if (currSuccess && lateSuccess) {

							// Need update
							if (latest > current) {
								print("There is a newer version of ", Color.ORANGE);
								print("Chat Room ", Color.ORANGERED);
								println("available.", Color.ORANGE);

								SimpleText text = new SimpleText();

								// Since this is a lambda expression, the object is not recreated each time.
								text.text.setOnMouseClicked(event -> {
									if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)
										updateProgram();

								});
								text.text.setFill(Color.WHITE);
								text.text.setUnderline(true);
								text.text.setText("Double click here");
								// I think TextFlows disable click on bounds, but whatever.
								text.text.setPickOnBounds(true);
								text.print(flow);

								println(" to download the update.", Color.ORANGE);
							} else
							// Fully updated
							if (latest == current) {
								println("You have the latest version. :D", SUCCESS_COLOR);
							} else
							// Above update...
							{
								println("Your version is above the latest, publicly released version. Congrats...?",
										INFO_COLOR);
							}

						}

					}
				}
			});

			// /set-name
			commandManager.addCommand(new Command() {

				final class SpecialConsoleText extends ConsoleText {

					public String text;

					public SpecialConsoleText(String text) {
						this.text = text;
					}

					@Override
					public void print(TextFlow flow) {
						flow.getChildren().addAll(println(), println(), println());
						for (char c : text.toCharArray()) {
							// The text
							Text t = new Text("" + c);

							// Default formatting
							formatText(t);

							// Assing some stuff
							t.setFill(new Color(Math.random(), Math.random(), Math.random(), 1));
							t.setFont(Font.font(t.getFont().getFamily(), Math.random() * 10 + 35)); // 35 ~ 45

							// Add a dropshadow
							DropShadow ds = new DropShadow();
							ds.setColor(new Color(Math.random(), Math.random(), Math.random(), 1));
							t.setEffect(ds);

							// Add it to the console.
							flow.getChildren().add(t);
						}
						flow.getChildren().addAll(println(), println(), println());
					}

				}

				@Override
				protected boolean match(String name) {
					return equalsAnyIgnoreCase(name, "setname", "set-name") || name.equals("sn");
				}

				@Override
				protected void act(String name, String... args) {
					if (args.length == 0) {
						println("I can't set your name unless you tell me what you want it to be... >:(", ERROR_COLOR);
						print("Usage: ", ERROR_COLOR);
						println("/set-name (name)", Color.ORANGE);
					} else {
						if (args.length > 1)
							println("You gave me too many arguments, so I'll just use the first one... That will be your name.....",
									ERROR_COLOR);
						if (args.length > 5) {
							print("Man, you really gave me an ", Color.DARKRED);
							print("excessive ", Color.CRIMSON);
							println("amount of arguments...", Color.DARKRED);
						}
						if (args.length > 15) {
							String chill = "CH";
							// Will iterate once if args.lenth==16, and once more for every one greater
							// than that.
							for (int i = 15; i < args.length; i++)
								chill += "I";
							chill += "LL";
							new SpecialConsoleText(chill).print(flow);

							println("With", Color.WHITE);
							println("The", Color.WHITE);
							println("Args.....", Color.WHITE);
							println();
							println("By the way, I didn't set your name. :)", ERROR_COLOR);
						}
						String username = args[0];
						ChatRoom.this.username = username;
					}
				}
			});

			commandManager.addCommand(new Command() {

				@Override
				protected boolean match(String name) {
					return name.equalsIgnoreCase("clients");
				}

				@Override
				protected void act(String name, String... args) {
					if (args.length > 0 && !args[0].equalsIgnoreCase("list"))
						println("This command doesn’t take any arguments.", Color.GOLD);

					if (clients.isEmpty()) {
						println("You have no registered clients.", ERROR_COLOR);
						return;
					}
					println("Here is a list of all the registered clients you have.", INFO_COLOR);
					boolean first = true;
					for (Client c : clients.values()) {
						if (!first)
							print(", ", SUCCESS_COLOR);
						else
							first = false;
						print(c.getName(), Color.WHITE);
					}
					println();

				}
			});

			commandManager.addCommand(new Command() {

				@Override
				protected boolean match(String name) {
					return name.equalsIgnoreCase("servers");
				}

				@Override
				protected void act(String name, String... args) {
					if (args.length > 0 && !args[0].equalsIgnoreCase("list"))
						println("This command doesn’t take any arguments.", Color.GOLD);

					if (servers.isEmpty()) {
						println("You have no registered servers.", ERROR_COLOR);
						return;
					}
					println("Here is a list of all the registered servers you have.", INFO_COLOR);
					boolean first = true;
					for (Server s : servers.values()) {
						if (!first)
							print(", ", SUCCESS_COLOR);
						else
							first = false;
						print(s.getName(), Color.WHITE);
					}
					println();
				}
			});

			commandManager.addCommand(new Command() {

				@Override
				protected boolean match(String name) {
					return name.equalsIgnoreCase("server") || name.equals("s");
				}

				@Override
				protected void act(String name, String... args) {
					if (args.length == 0) {
						print("Too few arguments. Usage: ", ERROR_COLOR);
						println("/" + name + " (subcommand)", Color.ORANGE);
						return;
					}

					final String subcommand = args[0];
					if (equalsAnyIgnoreCase(subcommand, "stop", "end", "end-connection", "close", "disconnect")) {

						if (servers.isEmpty()) {
							println("There are no running servers for you to close.", ERROR_COLOR);
							return;
						}
						final String serverName;
						if (args.length < 2)
							if (!servers.isItemSelected()) {
								print("You don't have a server selected. Did you want to close a specific server?\nUsage: ",
										ERROR_COLOR);
								println("/" + name + " " + subcommand + " [server-name]", Color.ORANGE);
								return;
							} else
								servers.removeItem(serverName = servers.getSelectedItem().getName());
						else {
							serverName = args[1];
							if (!servers.containsKey(serverName)) {
								print("There is no server by the name of ", ERROR_COLOR);
								print(serverName, Color.ORANGE);
								print(".", ERROR_COLOR);
								return;
							}
							servers.removeItem(serverName);
						}

						print("The server, ", SUCCESS_COLOR);
						print(serverName, Color.WHITE);
						print(", was removed successfully.", SUCCESS_COLOR);

					}
				}
			});

			commandManager.addCommand(new Command() {

				@Override
				protected boolean match(String name) {
					return name.equalsIgnoreCase("client") || name.equals("c");
				}

				@Override
				protected void act(String name, String... args) {
					if (args.length == 0) {
						print("Usage: ", ERROR_COLOR);
						println("/" + name + " (subcommand)", Color.ORANGE);
						return;
					}

					final String subcommand = args[0];
					if (equalsAnyIgnoreCase(subcommand, "stop", "end", "end-connection", "close", "disconnect")) {

						if (clients.isEmpty()) {
							println("There are no active clients for you to close.", ERROR_COLOR);
							return;
						}

						if (args.length < 2) {
							// No clientName specified. "/client stop"
							if (!clients.isItemSelected()) {
								print("You do not have a client selected. Did you mean to close a specific client?\nUsage: ",
										ERROR_COLOR);
								println("/" + name + " " + subcommand + " [client-name]", Color.ORANGE);
							} else {
								clients.removeItem(clients.getSelectedItem().getName());
							}

						} else {
							String clientName = args[1];
							if (clients.removeItem(clientName)) {
								print("The client with the name ", SUCCESS_COLOR);
								print(clientName, Color.WHITE);
								println(" was removed successfully!", SUCCESS_COLOR);
							} else {
								print("A command by the name of ", ERROR_COLOR);
								print(clientName, Color.ORANGE);
								println(" was not found.", ERROR_COLOR);
							}
						}
					} else if (subcommand.equalsIgnoreCase("select")) {
						if (args.length < 2) {
							println("You must specify what client you want me to select.", ERROR_COLOR);
							println("Usage: /client select (client-name)", ERROR_COLOR);
						} else {
							if (args.length > 2) {
								println("Too many arguments. Using only what is needed.", Color.GOLD);
								println("Usage: /client select (client-name)", Color.GOLD);
							}
							String clientName = args[1];
							if (clientName.equals(clients.getSelectedItem().getName()))
								println("That client is already selected.", Color.GOLD);
							else {

								if (!clients.containsKey(clientName)) {
									print("There isn't a client registered with the name ", ERROR_COLOR);
									println(clientName, Color.ORANGE);
								} else {
									clients.selectItem(clientName);
									println("Selected the specified client.", SUCCESS_COLOR);
								}
							}
						}
					} else if (subcommand.equalsIgnoreCase("list"))
						executeCommand("/clients list");

				}
			});

			// /help
			commandManager.addCommand(new Command() {

				@Override
				protected boolean match(String name) {
					return name.equalsIgnoreCase("help") || name.equals("?");
				}

				@Override
				protected void act(String name, String... args) {

					if (args.length == 0) {
						// TODO Print syntax rules first.
						printHelp(1);
						return;
					} else {
						// Check if the first arg is a number.
						HANDLE_HELP_PAGE: {

							for (char c : args[0].toCharArray())
								if (!Character.isDigit(c))
									break HANDLE_HELP_PAGE;
							Integer page = Integer.parseInt(args[0]);

							if (args.length > 1) {
								print("Ignoring additional args and displaying the help for page ", Color.GOLD);
								print("" + page, Color.WHITE);
								print(".", Color.GOLD);
							}

							printHelp(page);
							return;
						}
						// Handle help for a specific command
						String subcommand = args[0];
						if (subcommand.equalsIgnoreCase("new")) {
							if (args.length == 1) {
								// TODO Print stuff.
							}
						}
					}

				}

				public void printBasicHelp(String syntax, String description) {
					print(syntax, Color.CRIMSON);
					print(" - ", Color.WHITE);
					println(description, Color.DARKTURQUOISE);
				}

				public void printHelp(int page) {
					switch (page) {
					case 1:
						// /clear-screen
						printBasicHelp("/help [command]",
								"Provides help for a specific command (if provided) or all commands in general.");
						printBasicHelp("/clear-screen", "Clears all text (and other nodes) from the console.");
						// /new
						println("/new ...", Color.CRIMSON);
						printBasicHelp("\tclient (server-address) [port] (client-name)",
								"Creates a new client. The client will be connected to the server specified by (server-address). The port is optional and defaults to "
										+ DEFAULT_PORT
										+ ". The (client-name) is required and can be used to refer to the new client later.");
						printBasicHelp("\tserver [port] (server-name)",
								"Creates a new server with the given port. Do note that your router's firewall (if there is one) will likely block any incoming connections to your computer on any port, unless you port forward. The [port] is optional and defaults to "
										+ DEFAULT_PORT + ".");
						break;
					default:
						println("There is no help available for that page...", ERROR_COLOR);
					}
				}

			});

			// /clear-screen
			commandManager.addCommand(new Command() {

				@Override
				protected boolean match(String name) {
					return equalsAnyIgnoreCase(name, "cls", "clear-screen", "clearscreen");
				}

				@Override
				protected void act(String name, String... args) {
					flow.getChildren().clear();
				}
			});

			// /new
			commandManager.addCommand(new Command() {

				CommandManager argumentManager = new CommandManager();

				{
					// /new Client
					argumentManager.addCommand(new Command() {

						@Override
						protected boolean match(String name) {
							return name.equalsIgnoreCase("client") || name.equals("c");
						}

						@Override
						protected void act(String name, String... args) {
							if (args.length < 2) {
								print("Not enough arguments. Please input a server address and a name for the client. E.g., ",
										ERROR_COLOR);
								// Using 'name', we can get the exact command name that they put in, whether
								// they did /new client, or /new c.
								println("/new " + name + " dusttoash.org Test 25000", Color.ORANGE);
								print("dusttoash.org ", Color.ORANGE);
								print("would be the server address, and ", ERROR_COLOR);
								print("Test ", Color.ORANGE);
								println("would be the client's name.", ERROR_COLOR);
								print("See ", ERROR_COLOR);
								print("/help ", Color.ORANGE);
								println("for more information.", ERROR_COLOR);
							} else {

								final String hostname = args[0];
								int port = DEFAULT_PORT;
								final String clientName;

								try {

									Client client;

									// No port
									if (args.length == 2) {
										clientName = args[1];
									} else {
										if (args.length > 3)
											println("Too many arguments... Parsing only what is needed: (the first three args).",
													Color.GOLD);
										port = Integer.parseInt(args[2]);
										clientName = args[2];

									}

									client = new Client(hostname, port, clientName);

									// TODO The case of a taken name should be handled before the client is created.
									if (!clients.addItem(client)) {
										println("A client with this name already exists. Please choose a new name and try again.",
												ERROR_COLOR);
										client.closeConnection();
									} else if (!clients.isItemSelected()) {
										println("Since there is currently not a selected client, this new one that you've just created will be selected.",
												Color.CORNFLOWERBLUE);
										clients.selectItem(clientName);
									}
								} catch (NumberFormatException e) {
									println("The third argument could not be parsed as a port. The port must be a number between 0 and 65536, not inclusive. (So 15, 3500, and 65535 will work, but 0 and 65536 will not.)",
											ERROR_COLOR);
								} catch (UnknownHostException e) {
									println("The address could not be parsed as a valid server address, or the ip address of the host could not be determined.",
											ERROR_COLOR);
								} catch (ConnectException e) {
									print("There is no server listening for connections on the address ", ERROR_COLOR);
									print(hostname, Color.DARKRED);
									print(" and the port ", ERROR_COLOR);
									print("" + port, Color.DARKRED);
								} catch (IOException e) {
									println("Some kind of unknown error occurred while trying to connect.",
											ERROR_COLOR);
									e.printStackTrace();
								}
							}
						}
					});

					// /new Server
					argumentManager.addCommand(new Command() {

						@Override
						protected boolean match(String name) {
							return name.equalsIgnoreCase("server") || name.equals("s");
						}

						@Override
						protected void act(String name, String... args) {

							if (args.length < 1) {
								print("Too few arguments. See ", ERROR_COLOR);
								print("/help new " + name + " ", Color.ORANGE);
								println("for more info.", ERROR_COLOR);
								return;
							}

							try {

								final String serverName;
								final Integer port;

								Server server;

								// Handle 2+ args (name and port)
								if (args.length > 1) {
									if (args.length > 2)
										println("Too many arguments... Using only what is needed: (args 1 & 2).",
												Color.GOLD);
									serverName = args[1];
									port = Integer.parseInt(args[1]);

								} // Handle 1 arg (name)
								else {
									port = DEFAULT_PORT;
									serverName = args[0];
								}

								server = new Server(port, serverName);

								if (!servers.addItem(server)) {
									println("There already exists a server with the name " + serverName
											+ ". Please choose a different name and try again.", ERROR_COLOR);
									server.stop();
								}
								if (!servers.isItemSelected()) {
									servers.selectItem(serverName);
									println("You did not previously have a server selected, so the one you just made was selected automatically.",
											SUCCESS_COLOR);
								}

							} catch (NumberFormatException e) {
								println("Couldn't parse a port number for the server. The port must be a number between 0 and 65536, not inclusive. (So 15, 3500, and 65535 will work, for example, but 0 and 65536 will not.)",
										ERROR_COLOR);
							} catch (IOException e) {
								println("An error occurred while trying to host the server.", ERROR_COLOR);
							}
						}
					});
				}

				@Override
				protected boolean match(String name) {
					return name.equalsIgnoreCase("new");
				}

				@Override
				protected void act(String name, String... args) {
					if (args.length == 0) {
						print("No arguments specified. Do ", ERROR_COLOR);
						print("/new help ", Color.ORANGE);
						println("for help.", ERROR_COLOR);
					} else if (!argumentManager.runCommand(args)) {
						print("Unknown argument: ", ERROR_COLOR);
						println(args[0], Color.ORANGE);
					}
				}
			});

		}
		println("Done!", SUCCESS_COLOR);

		print("Connect to a server with ", ERROR_COLOR);
		print("/connect (URL:Hostname) [Int:Port] ", Color.CRIMSON);
		print("to get started!", ERROR_COLOR);
		println();
		print("Do ", Color.PURPLE);
		print("/help ", Color.WHITE);
		println("for more help.", Color.PURPLE);
		println();
		println();

	}

	private void print(String text, Color color) {
		new BasicInfoText(text, color).print(flow);
	}

	private void println() {
		new Println(flow);
	}

	private void println(String text, Color color) {
		print(text, color);
		println();
	}

	private void onUserSubmit() {
		String text = input.getText();

		// We don't want to handle nothing...
		if (text.isEmpty())
			return;

		// Given command.
		if (text.startsWith("/")) {
			// We only want to notify the user if the command was not recognized.
			if (!commandManager.runCommand(text))
				println("That command was not recognized.", Color.AQUA);
		}
		// Given message.
		else {

			if (clients.isItemSelected()) {
				clients.getSelectedItem().sendObject(new BasicUserMessage(username, text));
				new BasicUserText(username, text).print(flow);
			} else {
				print("You can only send messages to a server through a client. Do ", ERROR_COLOR);
				print("/new help ", Color.ORANGERED);
				println("For help with connections.", ERROR_COLOR);
			}

		}

		input.setText("");
	}

	private void updateProgram() {

		TRY_DOWNLOAD: {
			// Windows
			if (System.getProperty("os.name").toLowerCase().startsWith("win"))
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

	private void executeCommand(String command) {
		commandManager.runCommand(command);
	}

}
