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

import org.alixia.chatroom.api.Console;
import org.alixia.chatroom.api.Printable;
import org.alixia.chatroom.changelogparser.ChangelogParser;
import org.alixia.chatroom.commands.Command;
import org.alixia.chatroom.commands.CommandManager;
import org.alixia.chatroom.connections.Client;
import org.alixia.chatroom.connections.ClientManager;
import org.alixia.chatroom.connections.ConnectionListener;
import org.alixia.chatroom.connections.Server;
import org.alixia.chatroom.connections.ServerManager;
import org.alixia.chatroom.connections.messages.client.BasicUserMessage;
import org.alixia.chatroom.connections.messages.client.UserMessage;
import org.alixia.chatroom.fxtools.Resizable;
import org.alixia.chatroom.fxtools.ResizeOperator;
import org.alixia.chatroom.resources.fxnodes.FXTools;
import org.alixia.chatroom.resources.fxnodes.popbutton.PopButton;
import org.alixia.chatroom.texts.BasicInfoText;
import org.alixia.chatroom.texts.BasicUserText;
import org.alixia.chatroom.texts.ConsoleText;
import org.alixia.chatroom.texts.Println;
import org.alixia.chatroom.texts.SimpleText;

import javafx.animation.FillTransition;
import javafx.animation.StrokeTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
import javafx.util.Duration;

//TODO Later, I MIGHT put all the GUI nodes into a local class which in turn would go in the tryInit method (or something similar) and instantiate the console object when it has visibility of the nodes. Although this would severly decrease the clutter in my IDE, it would pose the problem of having GUI nodes not be accessible by the rest of the ChatRoom class, as they would be hidden in the local class, so commands will not be able to manipulate the nodes in the future without some serious remodeling.
//Also, this class is about 1530 lines. 90% of that is probably the commands...
/**
 * 
 * @author Zeale
 *
 */
public class ChatRoom {

	public static final Color ERROR_COLOR = Color.RED, INFO_COLOR = Color.LIGHTBLUE, SUCCESS_COLOR = Color.GREEN,
			WARNING_COLOR = Color.GOLD;

	private static final Color WINDOW_BORDER_COLOR = new Color(0.2, 0.2, 0.2, 1),
			NODE_OUTPUT_COLOR = new Color(0, 0, 0, 0.3), NODE_ITEM_COLOR = Color.DARKGRAY,
			WINDOW_BACKGROUND_COLOR = new Color(0.3, 0.3, 0.3, 0.8);

	private static final int DEFAULT_PORT = 25000;

	private String username = "Unnamed";

	private final Printable printer = new Printable() {

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

	private final Console console = new Console() {

		@Override
		public void printText(Text text) {
			flow.getChildren().add(text);
		}
	};

	// Nodes are styled and manipulated in the constructor and the tryInit method.
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
				Platform.runLater(() -> ((UserMessage) object).toConsoleText().print(console));

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

		flow.setBackground(FXTools.getBackgroundFromColor(NODE_OUTPUT_COLOR));
		input.setBackground(FXTools.getBackgroundFromColor(NODE_OUTPUT_COLOR));
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
		contentWrapper.setBackground(FXTools.getBackgroundFromColor(WINDOW_BACKGROUND_COLOR));

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

		Border border = new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(2, 2, 8, 2)));

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

				final double animationDuration = 0.2;

				{
					// Pos is the rect with a positive slope, neg is the negative sloped rect.
					Shape cross;
					{
						Shape pos = new Rectangle(24, 2), neg = new Rectangle(24, 2);
						neg.setRotate(45);
						pos.setRotate(-45);
						cross = Shape.union(pos, neg);
					}

					cross.setFill(ITEM_COLOR);
					cross.setStroke(ITEM_COLOR);
					cross.setStrokeWidth(1);

					StackPane.setAlignment(cross, Pos.CENTER);

					close.getChildren().add(cross);

					StrokeTransition stcross = new StrokeTransition(Duration.seconds(animationDuration), cross);
					FillTransition ftcross = new FillTransition(Duration.seconds(animationDuration), cross);

					close.setOnMouseEntered(event -> {
						stcross.stop();
						ftcross.stop();

						stcross.setFromValue((Color) cross.getStroke());
						stcross.setToValue(Color.RED);
						ftcross.setFromValue((Color) cross.getFill());
						ftcross.setToValue(Color.RED);

						stcross.play();
						ftcross.play();

					});

					close.setOnMouseExited(event -> {
						stcross.stop();
						ftcross.stop();

						stcross.setFromValue((Color) cross.getStroke());
						stcross.setToValue(ITEM_COLOR);
						ftcross.setFromValue((Color) cross.getFill());
						ftcross.setToValue(ITEM_COLOR);

						stcross.play();
						ftcross.play();

					});
				}

				{
					// Expand/Maximize
					Rectangle max = new Rectangle(20, 20);
					max.setFill(Color.TRANSPARENT);
					max.setStroke(ITEM_COLOR);
					max.setStrokeWidth(2.5);

					StackPane.setAlignment(max, Pos.CENTER);

					expand.getChildren().add(max);

					StrokeTransition stexp = new StrokeTransition(Duration.seconds(animationDuration), max);

					expand.setOnMouseMoved(event -> {
						stexp.stop();

						stexp.setFromValue((Color) max.getStroke());
						stexp.setToValue(Color.GREEN);

						stexp.play();
					});

					expand.setOnMouseExited(event -> {
						stexp.stop();

						stexp.setFromValue((Color) max.getStroke());
						stexp.setToValue(ITEM_COLOR);

						stexp.play();
					});

				}

				{
					// Minimize
					Rectangle min = new Rectangle(22, 2);
					StackPane.setAlignment(min, Pos.BOTTOM_CENTER);
					min.setFill(ITEM_COLOR);
					min.setStroke(ITEM_COLOR);
					min.setStrokeWidth(1);
					minimize.setPadding(new Insets(0, 0, 2, 0));
					minimize.getChildren().add(min);

					StrokeTransition stmin = new StrokeTransition(Duration.seconds(animationDuration), min);
					FillTransition ftmin = new FillTransition(Duration.seconds(animationDuration), min);

					minimize.setOnMouseEntered(event -> {
						stmin.stop();
						ftmin.stop();

						Color darkGold = new Color(1, 190d / 255, 0, 1);
						stmin.setFromValue((Color) min.getStroke());
						stmin.setToValue(darkGold);
						ftmin.setFromValue((Color) min.getFill());
						ftmin.setToValue(darkGold);

						stmin.play();
						stmin.play();
					});

					minimize.setOnMouseExited(event -> {
						stmin.stop();
						ftmin.stop();

						stmin.setFromValue((Color) min.getStroke());
						stmin.setToValue(ITEM_COLOR);
						ftmin.setFromValue((Color) min.getFill());
						ftmin.setToValue(ITEM_COLOR);

						stmin.play();
						stmin.play();
					});

				}
			} else {
				final double animationDuration = 0.3;
				Shape closeFill, minimizeFill, expandFill;
				closeFill = new Circle(size / 5);
				minimizeFill = new Circle(size / 5);
				expandFill = new Circle(size / 5);
				closeFill.setFill(Color.CORAL);
				expandFill.setFill(WARNING_COLOR);
				minimizeFill.setFill(Color.LIMEGREEN);
				close.getChildren().add(closeFill);
				minimize.getChildren().add(minimizeFill);
				expand.getChildren().add(expandFill);

				FillTransition ftclose = new FillTransition(Duration.seconds(animationDuration), closeFill),
						ftminimize = new FillTransition(Duration.seconds(animationDuration), minimizeFill),
						ftexpand = new FillTransition(Duration.seconds(animationDuration), expandFill);

				close.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
					ftclose.stop();
					ftclose.setFromValue((Color) closeFill.getFill());
					ftclose.setToValue(Color.WHITE);
					ftclose.play();
				});

				close.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
					ftclose.stop();
					ftclose.setFromValue((Color) closeFill.getFill());
					ftclose.setToValue(Color.CORAL);
					ftclose.play();
				});

				expand.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
					ftexpand.stop();
					ftexpand.setFromValue((Color) expandFill.getFill());
					ftexpand.setToValue(Color.WHITE);
					ftexpand.play();
				});

				expand.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
					ftexpand.stop();
					ftexpand.setFromValue((Color) expandFill.getFill());
					ftexpand.setToValue(WARNING_COLOR);
					ftexpand.play();
				});

				minimize.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
					ftminimize.stop();
					ftminimize.setFromValue((Color) minimizeFill.getFill());
					ftminimize.setToValue(Color.WHITE);
					ftminimize.play();
				});

				minimize.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
					ftminimize.stop();
					ftminimize.setFromValue((Color) minimizeFill.getFill());
					ftminimize.setToValue(Color.LIMEGREEN);
					ftminimize.play();
				});

			}

		}

		close.setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.PRIMARY)) {
				stage.close();
				Platform.exit();
			}
		});

		expand.setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.MIDDLE)) {
				stage.setMaximized(false);
				stage.setFullScreen(!stage.isFullScreen());
			} else if (event.getButton().equals(MouseButton.PRIMARY)) {
				if (stage.isFullScreen()) {
					stage.setMaximized(false);
					stage.setFullScreen(false);
				} else
					stage.setMaximized(!stage.isMaximized());
			}
		});

		minimize.setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.PRIMARY))
				stage.setIconified(true);
		});

		// Menu bar
		HBox menuBar = new HBox(OS.getOS() == OS.WINDOWS ? minimize : close, expand,
				OS.getOS() == OS.WINDOWS ? close : minimize);

		new Object() {

			private double dx, dy;

			{
				menuBar.setOnMousePressed(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						if (stage.isMaximized() || stage.isFullScreen())
							return;
						dx = stage.getX() - event.getScreenX();
						dy = stage.getY() - event.getScreenY();
						event.consume();
					}
				});

				menuBar.setOnMouseDragged(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						if (stage.isMaximized() || stage.isFullScreen())
							return;
						stage.setX(event.getScreenX() + dx);
						stage.setY(event.getScreenY() + dy);
						event.consume();
					}
				});
			}
		};

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
		new ResizeOperator(root, new Resizable() {

			@Override
			public void moveY(double amount) {
				stage.setY(stage.getY() + amount);
			}

			@Override
			public void moveX(double amount) {
				stage.setX(stage.getX() + amount);
			}

			@Override
			public double getY() {
				return stage.getY();
			}

			@Override
			public double getX() {
				return stage.getX();
			}

			@Override
			public void expandVer(double amount) {
				stage.setHeight(stage.getHeight() + amount);
			}

			@Override
			public void expandHor(double amount) {
				stage.setWidth(stage.getWidth() + amount);
			}
		}, 10) {

			public void addBar() {
				root.setBorder(new Border(
						new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(2, 2, 8, 2))));
			}

			public void removeBar() {
				root.setBorder(
						new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(2))));
			}

			@Override
			public void handle(MouseEvent event) {
				super.handle(event);
				if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED) && bottom(event))
					addBar();
				else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED))
					removeBar();
				else if (event.getEventType().equals(MouseEvent.MOUSE_MOVED)) {
					if (bottom(event))
						addBar();
					else
						removeBar();
				}

			}
		};
		// The input TextArea consumes mouse events, so we'll need to add a handler to
		// it too.

		input.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				root.fireEvent(event);
			}
		});

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
		// COMMANDS
		{

			abstract class ChatRoomCommand extends Command {
				protected void printHelp(String usage, String... descriptions) {
					print("Usage: " + usage, INFO_COLOR);
					print(" - ", Color.WHITE);
					for (String s : descriptions)
						println(s, SUCCESS_COLOR);
				}
			}

			commandManager.addCommand(new Command() {

				@Override
				protected boolean match(String name) {
					return name.startsWith("/");
				}

				@Override
				protected void act(String name, String... args) {
					String text = name.substring(1);
					for (String s : args)
						text += " " + s;
					sendText(text);
				}
			});

			commandManager.addCommand(new Command() {

				@Override
				protected boolean match(String name) {
					return name.equalsIgnoreCase("escape");
				}

				@Override
				protected void act(String name, String... args) {
					String text = "";
					for (String s : args)
						text += s + " ";
					sendText(text);

				}
			});

			commandManager.addCommand(new ChatRoomCommand() {

				@Override
				protected boolean match(String name) {
					return name.equalsIgnoreCase("changelog");
				}

				@Override
				protected void act(String name, String... args) {

					if (args.length == 0) {
						ChangelogParser parser = new ChangelogParser("/changelog.txt");
						parser.printChangelog(printer);

					} else {
						if (args.length > 1)
							println("Excessive args. Using only what is needed.", WARNING_COLOR);
						String arg = args[0];
						if (equalsHelp(arg)) {
							// TODO Change this code when moving to better versioning.
							printHelp("/" + name + " [version-number]",
									"Prints the changelog for the current version of the program (if no arguments are provided in the command), or the changelog of a specific version of this program (if an argument is provided and a matching version is found on the program's website).",
									"As of right now, versions are simply numbers, starting at one and going up. Later, versions may have more normal names, such as v0.1.7.2 or something. (The versioning format with periods is quite ubiquitous as of now.)");
						} else {
							int ver;
							try {
								// TODO Change this code when moving to better versioning.
								ver = Integer.parseInt(arg);
								URL location = new URL(
										"http://dusttoash.org/chat-room/changelogs/changelog-" + ver + ".txt");

								ChangelogParser parser = new ChangelogParser(location.openStream());
								print("Version: ", Color.MEDIUMAQUAMARINE);
								parser.printChangelog(printer);

							} catch (NumberFormatException e) {
								println("Failed to parse your argument, " + arg + " as a number.", ERROR_COLOR);
								return;
							} catch (MalformedURLException e) {
								println("Something went wrong when parsing the URL that was made to try and get data on the version you specified. This isn't a connection error.",
										ERROR_COLOR);
								e.printStackTrace();
								return;
							} catch (IOException e) {
								println("Failed to get the version data from the remote server.", ERROR_COLOR);
								e.printStackTrace();
							}

						}
					}
				}
			});

			commandManager.addCommand(new ChatRoomCommand() {

				@Override
				protected boolean match(String name) {
					return name.equalsIgnoreCase("update");
				}

				@Override
				protected void act(String name, String... args) {
					// Update the program

					if (args.length > 0) {

						String argument = args[0];
						if (argument.equalsIgnoreCase("force") || argument.equals("-f")) {
							println("Forcefully updating the program.", ERROR_COLOR);
							println("This ignores checks to see whether or not your version is the latest. If the update command is causing problems with version checking, this command is useful.",
									Color.CYAN);
							updateProgram();
							return;
						} else if (equalsHelp(argument)) {
							printHelp("/update [arg]",
									"The update command. Used to check for updates to ChatRoom or to update actually update ChatRoom. Use the \"force\" or \"-f\" arguments to force an \"update\" regardless of whether or not the server's version is newer.",
									"Running this command without arguments will check for an update and, if one is found, give you the option to install it by double clicking a link.");

							return;
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
								text.print(console);

								println(" to download the update.", Color.ORANGE);
							} else
							// Fully updated
							if (latest == current) {
								println("You have the latest version. :D", SUCCESS_COLOR);
							} else
							// Above update...
							{
								println("Your version is above the latest publicly released version. Congrats...?",
										INFO_COLOR);
							}

						}

					}
				}
			});

			// /set-name
			commandManager.addCommand(new ChatRoomCommand() {

				final class SpecialConsoleText extends ConsoleText {

					public String text;

					public SpecialConsoleText(String text) {
						this.text = text;
					}

					@Override
					public void print(Console console) {

						console.printAll(println(), println(), println());
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
							console.printText(t);
						}
						console.printAll(println(), println(), println());

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

						String arg = args[0];
						if (equalsHelp(arg)) {
							printHelp("/set-name (name)",
									"Sets your username in chat. This can be set anywhere and will take effect once you connect to a server with a client and send a message.");
							return;
						}

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
							new SpecialConsoleText(chill).print(console);

							println("With", Color.WHITE);
							println("The", Color.WHITE);
							println("Args.....", Color.WHITE);
							println();
							println("By the way, I didn't set your name. :)", ERROR_COLOR);
							return;
						}
						String username = args[0];
						ChatRoom.this.username = username;
						print("Your name was changed to ", INFO_COLOR);
						println(username, Color.CHARTREUSE);
					}
				}
			});

			commandManager.addCommand(new ChatRoomCommand() {

				@Override
				protected boolean match(String name) {
					return name.equalsIgnoreCase("clients");
				}

				@Override
				protected void act(String name, String... args) {
					if (args.length > 0)

						if (equalsHelp(args[0])) {
							printHelp("/clients ['list']",
									"Lists out all the clients you have available, by their names. The ['list'] option is optional and does nothing more. It must be typed literally, however, such as in ");
							print("/clients list", WARNING_COLOR);
							print(".", SUCCESS_COLOR);
							return;
						} else if (!args[0].equalsIgnoreCase("list"))
							println("This command doesn’t take any arguments.", WARNING_COLOR);

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

			commandManager.addCommand(new ChatRoomCommand() {

				@Override
				protected boolean match(String name) {
					return name.equalsIgnoreCase("servers");
				}

				@Override
				protected void act(String name, String... args) {
					if (args.length > 0)
						if (equalsHelp(args[0])) {
							printHelp("/servers ['list']",
									"Lists out all the servers you have available, by their names. The ['list'] option is optional and does nothing more. It must be typed literally, however, such as in ");
							print("/servers list", WARNING_COLOR);
							print(".", SUCCESS_COLOR);
							return;
						} else if (!args[0].equalsIgnoreCase("list"))
							println("This command doesn’t take any arguments.", WARNING_COLOR);

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

			commandManager.addCommand(new ChatRoomCommand() {

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
					if (equalsHelp(subcommand)) {
						printHelp("/" + name + " (subcommand)",
								"Allows you to modify or edit a server. You can edit the server that you have selected right now or you can modify a specific server by giving its name along with your command. If there is no selected server, you will need to provide a name.");
					} else if (equalsAnyIgnoreCase(subcommand, "stop", "end", "end-connection", "close",
							"disconnect")) {

						if (args.length > 1 && equalsHelp(args[1])) {
							printHelp("/server " + subcommand, "Stops a specific, or the currently selected server.");
							return;
						}

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

			commandManager.addCommand(new ChatRoomCommand() {

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

						if (equalsHelp(args[1])) {
							printHelp("/" + name + " " + subcommand + " [client-name]",
									"Stops a client and deletes it.");
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
							if (clients.isEmpty()) {
								println("There are no active clients for you to close.", ERROR_COLOR);
								return;
							}
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
							if (equalsHelp(args[1])) {
								printHelp("/" + name + " " + subcommand + " (client-name)",
										"Selects one of your registered clients given its name.");
								return;
							}
							if (args.length > 2) {
								println("Too many arguments. Using only what is needed.", WARNING_COLOR);
								println("Usage: /" + name + " " + subcommand + " (client-name)", WARNING_COLOR);
							}
							String clientName = args[1];
							if (clientName.equals(clients.getSelectedItem().getName()))
								println("That client is already selected.", WARNING_COLOR);
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
					} else if (subcommand.equalsIgnoreCase("list")) {
						if (equalsHelp(args[1])) {
							printHelp("/" + name + " " + subcommand,
									"Lists your registered clients. This command takes no arguments.");
							return;
						}
						executeCommand("/clients list");
					} else if (equalsHelp(subcommand)) {
						printHelp("/" + name + " (subcommand)",
								"Allows you to see information about or manipulate clients.");
						print("Possible subcommands: ", SUCCESS_COLOR);
						print("list", Color.WHITE);
						print(", ", SUCCESS_COLOR);
						print("select", Color.WHITE);
						print(", and ", SUCCESS_COLOR);
						print("stop", Color.WHITE);
						println(".", SUCCESS_COLOR);
					}

				}
			});

			// /help
			commandManager.addCommand(new ChatRoomCommand() {

				@Override
				protected boolean match(String name) {
					return name.equalsIgnoreCase("help") || name.equals("?");
				}

				@Override
				protected void act(String name, String... args) {

					if (args.length == 0) {

						println("Parentheses indicate a necessary parameter.", Color.PURPLE);
						println("Brackets indicate an unnecessary parameter.", Color.PURPLE);
						println("Elipses denote that a command has subcommands.", Color.PURPLE);

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
								print("Ignoring additional args and displaying the help for page ", WARNING_COLOR);
								print("" + page, Color.WHITE);
								print(".", WARNING_COLOR);
							}

							printHelp(page);
							return;
						}
						// Handle help for a specific command
						// This isn't supported by all commands
						String subcommand = args[0];
						if (subcommand.equalsIgnoreCase("new")) {
							if (args.length == 1) {
								printBasicHelp("/new (item)",
										"Creates a new (item). Some different types of (items) may require different arguments.");
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
			commandManager.addCommand(new ChatRoomCommand() {

				@Override
				protected boolean match(String name) {
					return equalsAnyIgnoreCase(name, "cls", "clear-screen", "clearscreen");
				}

				@Override
				protected void act(String name, String... args) {
					if (args.length > 0 && equalsHelp(args[0])) {
						printHelp("/" + name,
								"This simply clears all the items inside your console. Any text from commands or other users will be cleared.");
						return;
					}
					flow.getChildren().clear();
				}
			});

			// /new
			commandManager.addCommand(new ChatRoomCommand() {

				CommandManager argumentManager = new CommandManager();

				{
					// /new Client
					argumentManager.addCommand(new ChatRoomCommand() {

						@Override
						protected boolean match(String name) {
							return name.equalsIgnoreCase("client") || name.equals("c");
						}

						@Override
						protected void act(String name, String... args) {
							if (args.length > 0 && equalsHelp(args[0])) {
								printHelp("/new " + name + " (host-name) [port] (client-name)", "Creates a new " + name
										+ " given a (host-name), optionally a [port], and a (client-name).");
								return;
							}
							if (args.length < 2) {
								print("Not enough arguments. Please input a server address and a name for the client. E.g., ",
										ERROR_COLOR);
								// Using 'name', we can get the exact command name that they put in, whether
								// they did /new client, or /new c.
								println("/new " + name + " dusttoash.org 25000 MyClient", Color.ORANGE);
								print("dusttoash.org ", Color.ORANGE);
								print("would be the server address, and ", ERROR_COLOR);
								print("MyClient ", Color.ORANGE);
								println("would be this new client's name.", ERROR_COLOR);
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
													WARNING_COLOR);
										port = Integer.parseInt(args[1]);
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
									println("The second argument could not be parsed as a port. The port must be a number between 0 and 65536, not inclusive. (So 15, 3500, and 65535 will work, but 0 and 65536 will not.)",
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
					argumentManager.addCommand(new ChatRoomCommand() {

						@Override
						protected boolean match(String name) {
							return name.equalsIgnoreCase("server") || name.equals("s");
						}

						@Override
						protected void act(String name, String... args) {

							if (args.length < 1) {
								print("Too few arguments. See ", ERROR_COLOR);
								print("/new " + name + " help ", Color.ORANGE);
								println("for more info.", ERROR_COLOR);
								return;
							}

							try {

								final String serverName;
								final Integer port;

								Server server;

								if (args.length > 0 && equalsHelp(args[0])) {
									printHelp("/new " + name + " [port] (server-name)",
											"This command creates a new server with an optional [port] parameter and a (server-name). The (server-name) is just for you to modify the server later with the /server command. It does not appear to any users who connect or serve any purpose apart from reference.");
									return;
								}

								// Handle 2+ args (name and port)
								if (args.length > 1) {
									if (args.length > 2)
										println("Too many arguments... Using only what is needed: (args 1 & 2).",
												WARNING_COLOR);
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
					} else if (equalsHelp(args[0])) {
						printHelp("/" + name + " (item)",
								"This command lets you create new (items), such as clients or servers.",
								"Clients let you connect to a server and send or receive messages.",
								"Servers are what other people connect to using clients.",
								"To find more information about making/hosting a server, do /" + name + " server help",
								"To find more information about connecting to a server with a client, do /" + name
										+ " client help");
						return;
					} else if (!argumentManager.runCommand(args)) {
						print("Unknown argument: ", ERROR_COLOR);
						println(args[0], Color.ORANGE);
					}
				}
			});

		}
		println("Done!", SUCCESS_COLOR);

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
		println();
		println();

	}

	private void print(String text, Color color) {
		new BasicInfoText(text, color).print(console);
	}

	private void println() {
		new Println(console);
	}

	private void println(String text, Color color) {
		print(text, color);
		println();
	}

	/**
	 * Called when user pushes send.
	 */
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
			sendText(text);
		}

		input.setText("");
	}

	/**
	 * Sends text as the user.
	 * 
	 * @param text
	 *            The text.
	 */
	private void sendText(String text) {

		if (clients.isItemSelected()) {
			clients.getSelectedItem().sendObject(new BasicUserMessage(username, text));
			new BasicUserText(username, text).print(console);
		} else {
			print("You can only send messages to a server through a client. Do ", ERROR_COLOR);
			print("/new help ", Color.ORANGERED);
			println("For help with connections.", ERROR_COLOR);
		}

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
