package org.alixia.chatroom;

import java.io.IOException;
import java.net.UnknownHostException;

import org.alixia.chatroom.commands.Command;
import org.alixia.chatroom.commands.CommandManager;
import org.alixia.chatroom.connections.Client;
import org.alixia.chatroom.connections.ConnectionManager;
import org.alixia.chatroom.connections.Server;
import org.alixia.chatroom.connections.messages.client.BasicUserMessage;
import org.alixia.chatroom.texts.BasicInfoText;
import org.alixia.chatroom.texts.BasicUserText;
import org.alixia.chatroom.texts.Println;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class ChatRoom {
	private final static Background DEFAULT_NODE_BACKGROUND = new Background(
			new BackgroundFill(new Color(0.4, 0.4, 0.4, 0.7), null, null));

	private static final int DEFAULT_PORT = 25000;

	private String username = "Unnamed";

	// Some of these fields have aliases.
	private final TextFlow flow = new TextFlow();
	private final TextArea input = new TextArea();
	private final Button sendButton = new Button("Send");
	private final AnchorPane root = new AnchorPane(flow, input, sendButton);

	private final Scene scene = new Scene(root);
	private final Stage stage;

	private final CommandManager commandManager = new CommandManager();
	private final ConnectionManager connectionManager = new ConnectionManager();

	ChatRoom(Stage stage) {
		this.stage = stage;

		stage.setWidth(800);
		stage.setHeight(600);
		stage.show();

		flow.setBackground(DEFAULT_NODE_BACKGROUND);
		input.setBackground(DEFAULT_NODE_BACKGROUND);

		AnchorPane.setLeftAnchor(flow, 50d);
		AnchorPane.setRightAnchor(flow, 50d);
		AnchorPane.setTopAnchor(flow, 0d);
		AnchorPane.setBottomAnchor(flow, 250d);

		AnchorPane.setBottomAnchor(input, 0d);
		AnchorPane.setLeftAnchor(input, 0d);
		AnchorPane.setRightAnchor(input, 0d);

		AnchorPane.setRightAnchor(sendButton, 50d);
		AnchorPane.setBottomAnchor(sendButton, 88.5);

		input.setMaxHeight(200);

		// Add a ScrollPane to wrap flow
		root.setBackground(DEFAULT_NODE_BACKGROUND);

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

	private void tryInit() {
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

			// /help
			commandManager.commands.add(new Command() {

				@Override
				protected boolean match(String name) {
					return name.equalsIgnoreCase("help") || name.equals("?");
				}

				@Override
				protected void act(String name, String... args) {
					if (args.length == 0) {
						// TODO Print syntax rules first.

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
					}
				}

				public void printBasicHelp(String syntax, String description) {
					print(syntax, Color.CRIMSON);
					print(" - ", Color.WHITE);
					println(description, Color.DARKTURQUOISE);
				}

			});

			// /clear-screen
			commandManager.commands.add(new Command() {

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
			commandManager.commands.add(new Command() {

				CommandManager argumentManager = new CommandManager();

				{
					// /new Client
					argumentManager.commands.add(new Command() {

						@Override
						protected boolean match(String name) {
							return name.equalsIgnoreCase("client") || name.equals("c");
						}

						@Override
						protected void act(String name, String... args) {
							if (args.length < 2) {
								print("Not enough arguments. Please input a server address and a name for the client. E.g., ",
										Color.RED);
								// Using 'name', we can get the exact command name that they put in, whether
								// they did /new client, or /new c.
								println("/new " + name + " dusttoash.org Test 25000", Color.ORANGE);
								print("dusttoash.org ", Color.ORANGE);
								print("would be the server address, and ", Color.RED);
								print("Test ", Color.ORANGE);
								println("would be the client's name.", Color.RED);
								print("See ", Color.RED);
								print("/help ", Color.ORANGE);
								println("for more information.", Color.RED);
							} else {
								try {

									Client client;

									final String hostname = args[0];
									final int port;
									final String clientName;

									// No port
									if (args.length == 2) {
										port = DEFAULT_PORT;
										clientName = args[1];
									} else {
										if (args.length > 3)
											println("Too many arguments... Parsing only what is needed: (the first three args).",
													Color.GOLD);
										port = Integer.parseInt(args[2]);
										clientName = args[2];

									}

									client = new Client(hostname, port);

									// TODO The case of a taken name should be handled before the client is created.
									if (!connectionManager.addClient(clientName, client)) {
										println("A client with this name already exists. Please choose a new name and try again.",
												Color.RED);
										client.closeConnection();
									} else if (!connectionManager.isClientSelected()) {
										println("Since there is currently not a selected client, this new one that you've just created will be selected.",
												Color.CORNFLOWERBLUE);
										connectionManager.selectClient(clientName);
									}
								} catch (NumberFormatException e) {
									println("The third argument could not be parsed as a port. The port must be a number between 0 and 65536, not inclusive. (So 15, 3500, and 65535 will work, but 0 and 65536 will not.)",
											Color.RED);
								} catch (UnknownHostException e) {
									println("The address could not be parsed as a valid server address, or the ip address of the host could not be determined.",
											Color.RED);
								} catch (IOException e) {
									println("Some kind of unknown error occurred while trying to connect.", Color.RED);
									e.printStackTrace();
								}
							}
						}
					});

					// /new Server
					argumentManager.commands.add(new Command() {

						@Override
						protected boolean match(String name) {
							return name.equalsIgnoreCase("Server") || name.equals("s");
						}

						@Override
						protected void act(String name, String... args) {

							if (args.length < 1) {
								print("Too few arguments. See ", Color.RED);
								print("/help new " + name + " ", Color.ORANGE);
								println("for more info.", Color.RED);
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

								server = new Server(port);

								if (!connectionManager.addServer(serverName, server)) {
									println("There already exists a server with the name " + serverName
											+ ". Please choose a different name and try again.", Color.RED);
									server.stop();
								}
								if (!connectionManager.isServerSelected()) {
									connectionManager.selectServer(serverName);
									println("You did not previously have a server selected, so the one you just made was selected automatically.",
											Color.GREEN);
								}

							} catch (NumberFormatException e) {
								println("Couldn't parse a port number for the server. The port must be a number between 0 and 65536, not inclusive. (So 15, 3500, and 65535 will work, for example, but 0 and 65536 will not.)",
										Color.RED);
							} catch (IOException e) {
								println("An error occurred while trying to host the server.", Color.RED);
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
						print("No arguments specified. Do ", Color.RED);
						print("/new help ", Color.ORANGE);
						println("for help.", Color.RED);
					} else if (!argumentManager.runCommand(args)) {
						print("Unknown argument: ", Color.RED);
						println(args[0], Color.ORANGE);
					}
				}
			});

		}
		println("Done!", Color.GREEN);

		print("Connect to a server with ", Color.RED);
		print("/connect (URL:Hostname) [Int:Port] ", Color.CRIMSON);
		print("to get started!", Color.RED);
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

			if (connectionManager.isClientSelected()) {
				connectionManager.getCurrentClient().sendObject(new BasicUserMessage(username, text));
				new BasicUserText(username, text).print(flow);
			} else {
				print("You can only send messages to a server through a client. Do ", Color.RED);
				print("/new help ", Color.ORANGERED);
				println("For help with connections.", Color.RED);
			}

		}

		input.setText("");
	}

}
