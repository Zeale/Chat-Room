package org.alixia.chatroom.impl.commands;

import static org.alixia.chatroom.ChatRoom.DEFAULT_CALL_PORT;
import static org.alixia.chatroom.ChatRoom.DEFAULT_CALL_SAMPLE_RATE;
import static org.alixia.chatroom.ChatRoom.DEFAULT_CHAT_PORT;
import static org.alixia.chatroom.ChatRoom.ERROR_COLOR;
import static org.alixia.chatroom.ChatRoom.INFO_COLOR;
import static org.alixia.chatroom.ChatRoom.SUCCESS_COLOR;
import static org.alixia.chatroom.ChatRoom.WARNING_COLOR;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.Console;
import org.alixia.chatroom.api.OS;
import org.alixia.chatroom.api.Printable;
import org.alixia.chatroom.api.UserData;
import org.alixia.chatroom.changelogparser.ChangelogParser;
import org.alixia.chatroom.commands.Command;
import org.alixia.chatroom.commands.CommandConsumer;
import org.alixia.chatroom.commands.CommandManager;
import org.alixia.chatroom.connections.Client;
import org.alixia.chatroom.connections.ClientManager;
import org.alixia.chatroom.connections.Server;
import org.alixia.chatroom.connections.ServerManager;
import org.alixia.chatroom.connections.voicecall.CallClient;
import org.alixia.chatroom.connections.voicecall.CallServer;
import org.alixia.chatroom.internet.Authentication;
import org.alixia.chatroom.internet.authmethods.AuthenticationMethod.LoginResult;
import org.alixia.chatroom.texts.BasicInfoText;
import org.alixia.chatroom.texts.ConsoleText;
import org.alixia.chatroom.texts.Println;
import org.alixia.chatroom.texts.SimpleText;

import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public final class Commands {

	// Literally everything in this class is implementation specific, so it
	// DEFINITELY belongs in this impl class.

	/**
	 * This is actually, conveniently, used to load this class (and, thus, all of
	 * the commands) at startup.
	 * 
	 * @return The time (after all static initialization has taken place).
	 */
	public static long getTime() {
		return System.currentTimeMillis();
	}

	private Commands() {
	}

	private static final CommandManager commandManager = ChatRoom.INSTANCE.commandManager;
	private static final CallServer callServer = ChatRoom.INSTANCE.getCallServer();
	private static final CallClient callClient = ChatRoom.INSTANCE.getCallClient();
	private static final ClientManager clients = ChatRoom.INSTANCE.clients;
	private static final ServerManager servers = ChatRoom.INSTANCE.servers;
	private static Printable printer = ChatRoom.INSTANCE.printer;
	private static Console console = ChatRoom.INSTANCE.console;

	public static void sendText(String text) {
		ChatRoom.INSTANCE.sendText(text);
	}

	public static final Command AUTH_SERVER = new ChatRoomCommand() {

		@Override
		protected boolean match(String name) {
			// Not ignorecase
			return equalsAny(name, "auth-server", "authserver");
		}

		@Override
		protected void act(String name, String... args) {
			if (args.length == 0)
				println(Authentication.getDefaultAuthenticationMethod().toString(), Color.DARKORANGE);
			else {
				String subcommand = args[0];
				if (subcommand.equalsIgnoreCase("add")) {
					if (args.length < 3) {
						print("Usage: ", ERROR_COLOR);
						println("/" + name + " " + subcommand + " (username) (password)", ERROR_COLOR);
						return;
					}
					try {
						Authentication.getAuthServer().addUser(args[1], args[2]);
						println("Successfully added " + args[1], SUCCESS_COLOR);
					} catch (Exception e) {
						println("An error occurred while trying to add " + args[1], ERROR_COLOR);
						return;
					}
				} else if (subcommand.equalsIgnoreCase("save")) {
					if (args.length < 2) {
						print("Usage: ", ERROR_COLOR);
						println("/" + name + " " + subcommand + " (file-path.extension)", ERROR_COLOR);
						return;
					}
					File file = new File(args[1]);
					if (file.exists())
						file.delete();
					try {
						Authentication.getAuthServer().store(file);
					} catch (IOException e) {
						println("Failed to create the file: " + file.getAbsolutePath() + "."
								+ (file.exists() ? " Note that this is probably NOT because the file exists." : ""),
								ERROR_COLOR);
						e.printStackTrace();
						return;
					} catch (Exception e) {
						println("Failed to print data to the file: " + file.getAbsolutePath(), ERROR_COLOR);
						return;
					}
					println("Successfully printed the user data to the file, " + file.getAbsolutePath(), SUCCESS_COLOR);
				}
			}
		}
	};

	public static final Command LOGIN = new ChatRoomCommand() {

		private String accountName, password;
		private final CommandConsumer usernameConsumer = new CommandConsumer() {

			@Override
			public void consume(String command, String... args) {
				if (command.equals("cancel") && args.length == 0)
					return;

				if (command.isEmpty()) {

					if (accountName == null)
						println("Please enter a username.", ERROR_COLOR);
					else if (password == null)
						println("Please enter a password.", ERROR_COLOR);
					addConsumer(this);
					return;
				}
				if (accountName == null) {
					accountName = command;
					if (args.length > 0)
						password = args[0];
					else
						addConsumer(this);
				} else if (password == null)
					password = command;

			}
		};

		private final Runnable login = new Runnable() {

			@Override
			public void run() {
				LoginResult result;
				try {
					result = Authentication.getDefaultAuthenticationMethod().login(accountName, password);
				} catch (IOException e) {
					e.printStackTrace();
					println("An error occurred while trying to log in.", ERROR_COLOR);
					return;
				}

				if (result.sessionID == null) {
					switch (result.errType) {
					case TIMEOUT:
						println("The server could not be connected to.", ERROR_COLOR);
						break;
					case USERNAME_NOT_FOUND:
						println("That username was not found by the server.", ERROR_COLOR);
						break;
					case WRONG_PASSWORD:
						println("That password was invalid.", ERROR_COLOR);
						break;
					default:
						break;
					}
					return;
				}

				ChatRoom.INSTANCE.userData = new UserData(accountName, result.sessionID);

				accountName = null;
				password = null;

			}
		};

		@Override
		protected boolean match(String name) {
			return name.equalsIgnoreCase("login");
		}

		@Override
		protected void act(String name, String... args) {
			if (args.length > 0) {
				if (args.length > 1) {
					password = args[1];
					login.run();
				} else {
					println("Please enter a password:", INFO_COLOR);
					addConsumer(usernameConsumer);
				}
				accountName = args[0];
			} else {
				println("Please enter a username:", INFO_COLOR);
				addConsumer(usernameConsumer);
			}
		}
	};

	public static final Command OPEN_SETTINGS = new ChatRoomCommand() {

		@Override
		protected boolean match(String name) {
			return name.equalsIgnoreCase("settings");
		}

		@Override
		protected void act(String name, String... args) {
			if (args.length > 0) {
				if (equalsHelp(args[0])) {
					printHelp("/" + name,
							"Opens up the settings window. This allows you to customize program settings and/or login.");
					println("Would you like to open the settings window? (Y/N)", INFO_COLOR);
					addConsumer(new CommandConsumer() {

						@Override
						public void consume(String command, String... args) {
							if (equalsAnyIgnoreCase(command, "yes", "y")) {
								println("Opening settings window...", SUCCESS_COLOR);
								// Try block is *currently* useless
								try {
									openSettingsWindow();
								} catch (Exception e) {
									println("Failed to open window...", ERROR_COLOR);
								}
								return;
							} else if (equalsAnyIgnoreCase(command, "no", "n")) {
								println("Ok.", INFO_COLOR);
								return;
							} else {
								print("Unknown answer. Please enter either ", ERROR_COLOR);
								print("/Yes", SUCCESS_COLOR);
								print(" or ", ERROR_COLOR);
								print("/No", SUCCESS_COLOR);
								println(".", ERROR_COLOR);
								addConsumer(this);
								return;
							}
						}
					});
				}
			} else {
				println("Opening settings window...", SUCCESS_COLOR);
				// Try block is *currently* useless
				try {
					openSettingsWindow();
				} catch (Exception e) {
					println("Failed to open window...", ERROR_COLOR);
				}
			}
		}
	};

	public static final Command HOST_CALL = new ChatRoomCommand() {

		@Override
		protected boolean match(String name) {
			return equalsAnyIgnoreCase(name, "host-call", "hostcall");
		}

		@Override
		protected void act(String name, String... args) {

			if (callServer != null) {
				print("There is already an active server. Do ", ERROR_COLOR);
				print("/" + name + " close ", Color.ORANGE);
				println("to close the current call server.", ERROR_COLOR);
				return;
			}

			if (args.length > 0) {
				if (equalsHelp(args[0])) {
					printHelp("/" + name + " [port]",
							"Starts a call server. This is a voice chatting server that others can join.",
							"Running this command does not put you in the server; you must run   /call self   to join your own call.");
					return;
				}

				if (args[0].equalsIgnoreCase("close")) {
					if (callServer != null) {
						try {
							callServer.stop();
						} catch (IOException e) {
							println("A data streaming exception occurred while trying to close the server.",
									ERROR_COLOR);
							e.printStackTrace();
						}
						ChatRoom.INSTANCE.setCallServer(null);
					} else
						println("You aren't hosting a call...", ERROR_COLOR);
					return;
				}
			}

			try {
				ChatRoom.INSTANCE.setCallServer(
						new CallServer(args.length == 0 ? DEFAULT_CALL_PORT : Integer.parseInt(args[0])));
			} catch (IOException e) {
				println("An error occurred while trying to create a server.", ERROR_COLOR);
				e.printStackTrace();
				return;
			} catch (NumberFormatException e) {
				println("The port you entered could not be parsed as a number.", ERROR_COLOR);
				return;
			}

			println("Successfully started hosting a call.", SUCCESS_COLOR);
		}

	};

	public static final Command CALL = new ChatRoomCommand() {

		@Override
		protected boolean match(String name) {
			return name.equalsIgnoreCase("call");
		}

		@Override
		protected void act(String name, String... args) {

			if (args[0].equalsIgnoreCase("disconnect")) {
				try {
					if (callClient == null) {
						print("There is no active call for you to disconnect...", ERROR_COLOR);
					}
					callClient.disconnect();
					ChatRoom.INSTANCE.setCallClient(null);
				} catch (IOException e) {
					e.printStackTrace();
					println("A data streaming error occurred while trying to disconnect the client.", ERROR_COLOR);
				}
				return;
			}

			if (callClient != null) {
				print("There is already a call active. Do ", ERROR_COLOR);
				print("/" + name + " disconnect ", Color.ORANGE);
				println("to disconnect from the current call.", ERROR_COLOR);
				println("(Note that you might not be in a call, but if you created one before and haven't cleared it yet, you can't create a new one, regardless of whether or not the previous call failed.)",
						WARNING_COLOR);
				return;
			}

			if (args.length < 1)
				println("Please enter an address...", ERROR_COLOR);

			if (equalsHelp(args[0])) {
				printHelp("/" + name + " (server-address) [audio-level]",
						"Calls a callserver with the specified address.",
						"The (server-address) is the internet url or ip that is used to connect to the server.",
						"The [audio-level] is not a required parameter, but can be given. The [audio-level] allows you to specify the quality of the audio being sent to and from the server.",
						"I'm not too sure how this program works, but I don't think that anyone else will be able to hear you if they have different audio levels and they try to join the same call. Having different people in the same call with different audio levels may make some of them have to restart the program (or something). Again, not too big of a deal, but...",
						"There are 6 preset audio levels which can be selected by passing \"l\" (without quotes) and then a number from 1-6. Examples:",
						"\"l2\"", "\"l1\"", "\"l6\"",
						"You can also directly specify the audio level by simply entering a number without an \"l\" infront of it.",
						"The audio level is the sample rate of the sound data streamed to others in the call.");
				return;
			}

			try {
				String location = args[0];
				float sampleRate = DEFAULT_CALL_SAMPLE_RATE;

				if (equalsAnyIgnoreCase(args[0], "self", "s"))
					location = "localhost";
				if (args.length > 1) {
					final String rate = args[1];
					if (rate.toLowerCase().startsWith("l")) {
						int level;
						try {
							level = Integer.parseInt(rate.substring(1));
							switch (level) {
							case 1:
								sampleRate = 8000;
								break;
							case 2:
								sampleRate = 12000;
							case 3:
								sampleRate = 24000;
							case 4:
								sampleRate = 48000;
							case 5:
								sampleRate = 96000;
							case 6:
								sampleRate = 192000;
							default:
								sampleRate = DEFAULT_CALL_SAMPLE_RATE;
								break;
							}
						} catch (NumberFormatException e) {
							println("Could not parse an audio level preset...", ERROR_COLOR);
							println("Usage: /" + name + " " + args[0] + " l[level]", Color.ORANGE);
							return;
						}
					} else {
						try {
							sampleRate = Float.parseFloat(rate);
						} catch (NumberFormatException e) {
							println("Could not parse an audio level...", ERROR_COLOR);
							println("Usage: /" + name + " " + args[0] + " [number]", Color.ORANGE);
							return;
						}
					}
				}

				ChatRoom.INSTANCE.setCallClient(
						new CallClient(location, DEFAULT_CALL_PORT, new AudioFormat(sampleRate, 16, 1, true, true)));
			} catch (LineUnavailableException e) {
				println("Failed to make the call client. Your microphone could not be accessed...", ERROR_COLOR);
				e.printStackTrace();
			} catch (UnknownHostException e) {
				println("Failed to connect to the server. The server could not be found (i.e. its address could not be determined).",
						ERROR_COLOR);
			} catch (IOException e) {
				println("Failed to connect to the server due to some data streaming error.", ERROR_COLOR);
				e.printStackTrace();
			}

		}
	};

	public static final Command _ESCAPE = new Command() {

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
	};

	public static final Command ESCAPE = new Command() {

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
	};

	public static final Command CHANGELOG = new ChatRoomCommand() {

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
						URL location = new URL("http://dusttoash.org/chat-room/changelogs/changelog-" + ver + ".txt");

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
	};

	public static final Command UPDATE = new ChatRoomCommand() {

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
					println("The version of your copy of this application could not be determined.", ERROR_COLOR);
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
						println("Your version is above the latest publicly released version. Congrats...?", INFO_COLOR);
					}

				}

			}
		}
	};

	public static final Command SET_NAME = new ChatRoomCommand() {

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

				if (isLoggedIn()) {
					println("You can't change your name if you're logged in.", ERROR_COLOR);
					println("This feature may be released in a later update.", INFO_COLOR);
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
				ChatRoom.INSTANCE.setUsername(username);
				print("Your name was changed to ", INFO_COLOR);
				println(username, Color.CHARTREUSE);
			}
		}
	};

	public static final Command CLIENTS = new ChatRoomCommand() {

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
					println("This command doesn�t take any arguments.", WARNING_COLOR);

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
	};

	public static final Command SERVERS = new ChatRoomCommand() {

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
					println("This command doesn�t take any arguments.", WARNING_COLOR);

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
	};

	public static final Command SERVER = new ChatRoomCommand() {

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
			} else if (equalsAnyIgnoreCase(subcommand, "stop", "end", "end-connection", "close", "disconnect")) {

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
	};

	public static final Command CLIENT = new ChatRoomCommand() {

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
					printHelp("/" + name + " " + subcommand + " [client-name]", "Stops a client and deletes it.");
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
				printHelp("/" + name + " (subcommand)", "Allows you to see information about or manipulate clients.");
				print("Possible subcommands: ", SUCCESS_COLOR);
				print("list", Color.WHITE);
				print(", ", SUCCESS_COLOR);
				print("select", Color.WHITE);
				print(", and ", SUCCESS_COLOR);
				print("stop", Color.WHITE);
				println(".", SUCCESS_COLOR);
			}

		}
	};

	public static final Command HELP = new ChatRoomCommand() {

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
								+ DEFAULT_CHAT_PORT
								+ ". The (client-name) is required and can be used to refer to the new client later.");
				printBasicHelp("\tserver [port] (server-name)",
						"Creates a new server with the given port. Do note that your router's firewall (if there is one) will likely block any incoming connections to your computer on any port, unless you port forward. The [port] is optional and defaults to "
								+ DEFAULT_CHAT_PORT + ".");
				break;
			default:
				println("There is no help available for that page...", ERROR_COLOR);
			}
		}

	};

	public static final Command CLEAR_SCREEN = new ChatRoomCommand() {

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
			ChatRoom.INSTANCE.getGUI().flow.getChildren().clear();
		}
	};

	public static final Command NEW = new ChatRoomCommand() {

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
								+ " given a (host-name), optionally, a [port], and a (client-name).");
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
						int port = DEFAULT_CHAT_PORT;
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

							if (isLoggedIn()) {
								client.sendObject(ChatRoom.INSTANCE.userData);
							}

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
							println("Some kind of unknown error occurred while trying to connect.", ERROR_COLOR);
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
							port = DEFAULT_CHAT_PORT;
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
	};

	private static void executeCommand(String command) {
		commandManager.runCommand(command);
	}

	private static boolean isLoggedIn() {
		return ChatRoom.INSTANCE.userData != null;
	}

	private static void openSettingsWindow() {
		ChatRoom.INSTANCE.settingsInstance.get().show();
	}

	private static void print(String text, Color color) {
		new BasicInfoText(text, color).print(console);
	}

	private static void println() {
		new Println(console);
	}

	private static void println(String text, Color color) {
		print(text, color);
		println();
	}

	private static void updateProgram() {

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
