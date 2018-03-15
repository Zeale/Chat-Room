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
import java.io.FileNotFoundException;
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
import java.util.Collection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.Account;
import org.alixia.chatroom.api.Console;
import org.alixia.chatroom.api.OS;
import org.alixia.chatroom.api.Printable;
import org.alixia.chatroom.api.changelogparser.ChangelogParser;
import org.alixia.chatroom.api.commands.Command;
import org.alixia.chatroom.api.commands.CommandConsumer;
import org.alixia.chatroom.api.commands.CommandManager;
import org.alixia.chatroom.api.connections.voicecall.CallClient;
import org.alixia.chatroom.api.connections.voicecall.CallServer;
import org.alixia.chatroom.api.internet.Authentication;
import org.alixia.chatroom.api.internet.BasicAuthServer.User;
import org.alixia.chatroom.api.internet.BasicAuthServer.UserDataParseException;
import org.alixia.chatroom.api.internet.authmethods.exceptions.AccountCreationDeniedException;
import org.alixia.chatroom.api.internet.authmethods.exceptions.InvalidSessionIDException;
import org.alixia.chatroom.api.internet.authmethods.exceptions.InvalidUsernameException;
import org.alixia.chatroom.api.internet.authmethods.exceptions.TimeoutException;
import org.alixia.chatroom.api.internet.authmethods.exceptions.UnknownAuthenticationException;
import org.alixia.chatroom.api.internet.authmethods.exceptions.UsernameNotFoundException;
import org.alixia.chatroom.api.internet.authmethods.exceptions.UsernameTakenException;
import org.alixia.chatroom.api.logging.Logger;
import org.alixia.chatroom.api.texts.BasicInfoText;
import org.alixia.chatroom.api.texts.BoldText;
import org.alixia.chatroom.api.texts.ConsoleText;
import org.alixia.chatroom.api.texts.Println;
import org.alixia.chatroom.api.texts.SimpleText;

import javafx.application.Platform;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public final class Commands {

	// Literally everything in this class is implementation specific, so it
	// DEFINITELY belongs in this impl class.

	private static final CommandManager commandManager = ChatRoom.INSTANCE.commandManager;

	private static Printable printer = ChatRoom.INSTANCE.printer;
	private static Console console = ChatRoom.INSTANCE.console;

	public static final Command HELP = new ChatRoomCommand() {

		@Override
		protected void act(final String name, final String... args) {

			if (args.length == 0) {

				println("Parentheses indicate a necessary parameter.", Color.PURPLE);
				println("Brackets indicate an unnecessary parameter.", Color.PURPLE);
				println("Elipses denote that a command has subcommands.", Color.PURPLE);

				printHelp(1);
				return;
			} else {
				// Check if the first arg is a number.
				HANDLE_HELP_PAGE: {

					for (final char c : args[0].toCharArray())
						if (!Character.isDigit(c))
							break HANDLE_HELP_PAGE;

					final Integer page;
					page = Integer.parseInt(args[0]);

					if (args.length > 1) {
						print("Ignoring additional args and displaying the help for page ", WARNING_COLOR);
						print("" + page, Color.WHITE);
						print(".", WARNING_COLOR);
					}

					printHelp(page);
					return;
				}

				String cmd = "/";
				for (String s : args)
					cmd += s + " ";
				if (!commandManager.runCommand(cmd + "help"))
					println("Help for that command could not be found...", ERROR_COLOR);

			}

		}

		@Override
		protected boolean match(final String name) {
			return name.equalsIgnoreCase("help") || name.equals("?");
		}

		public void printBasicHelp(final String syntax, final String description) {
			print(syntax, Color.CRIMSON);
			print(" - ", Color.WHITE);
			println(description, Color.DARKTURQUOISE);
		}

		public void printHelp(final int page) {
			switch (page) {
			case 1:
				println("SHOWING PAGE 1 OF HELP", Color.BISQUE);
				// /clear-screen
				printBasicHelp("/help page",
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
			case 2:
				println("SHOWING PAGE 2 OF HELP", Color.BISQUE);
				printBasicHelp("/cleanup", "Attempts to free up ram by clearing unused program resources.");
				printBasicHelp("/client [subcommand]", "Manage your clients...");
				printBasicHelp("/server [subcommand]", "Manage your servers...");
				printBasicHelp("/set-name (name)", "Changes your name to (name).");
				break;
			case 3:
				println("SHOWING PAGE 3 OF HELP", Color.BISQUE);
				printBasicHelp("/update",
						"Gives information about this version of the program and the latest public version. If this command finds that an update is available, you will be able to download it.");
				printBasicHelp("/changelog (version)",
						"Displays the changelog for this version of the program or for version (version), if specified.");
				printBasicHelp("/call (address) [port]",
						"Calls the specified (address). If a [port] is given, the (address) is called on the specified [por].");
				break;
			case 4:
				println("SHOWING PAGE 4 OF HELP", Color.BISQUE);
				printBasicHelp("/settings", "Opens the settings page.");
				printBasicHelp("/auth-server [subcommand]",
						"Allows you to manage your authentication server, if you have one running. This is primarily used for the site that hosts authentication for users of this program. (The only reason that someone other than the application developer would have an auth server running is if the developer's server went down permanently. Otherwise, the developer's server should be used for authentication.)");
				break;
			case 5:
				println("SHOWING PAGE 5 OF HELP", Color.BISQUE);
				printBasicHelp("/login [username] [password]",
						"This command allows you to log in to your account. If a password isn't specified, it will be prompted for. If not a username or a password is specified, both will be prompted for. (P.S. You can't enter a password without a username straight into this command; it will be taken as your username.)");
				break;
			default:
				println("There is no help available for that page...", ERROR_COLOR);
			}
		}

	};

	public static final Command AUTH_SERVER = new ChatRoomCommand() {

		@Override
		protected void act(final String name, final String... args) {
			if (args.length == 0)
				if (Authentication.isAuthServerRunning())
					println(Authentication.getAuthServer().toString(), Color.DARKORANGE);
				else
					println("There is no Authentication Server running.", ERROR_COLOR);
			else {
				final String subcommand = args[0];
				if (equalsHelp(subcommand)) {
					printHelp("/" + name + " [subcommand]",
							"Allows you to manage your Authentication Server or see information about it (if it's running).");
					printSubcommandHelp(name, "help", "Prints the help for authentication server commands.");
					printSubcommandHelp(name, "add (username) (password)",
							"Adds an account to the authentication server. After running this command, the user will be able to log in with the given information.");
					printSubcommandHelp(name, "save (file-path.extension)",
							"Saves the accounts in this Authentication Server to a file. This way, it can be loaded the next time this program is run and users can log in with their account information.",
							"Example: /" + name + " save C:/Users/Username/Desktop/Data.abc",
							"The data can be loaded back into the program with /" + name
									+ " load (file-path.extension)");
					printSubcommandHelp(name, "load (file-path.extension)",
							"Loads the user data from a file into the running authentication server. This data can then be used to log in by users connecting to the authentication server. NOTICE: Read the help in   /"
									+ name
									+ " load help   before running the command; this command will clear any currently loaded accounts.");
					printSubcommandHelp(name, "print-users",
							"Prints the usernames of all the users inside the authentication server.");
				} else if (subcommand.equalsIgnoreCase("add")) {
					if (args.length > 1 && equalsHelp(args[1])) {
						printHelp("/" + name + " " + subcommand + " (username) (password)",
								"This command lets you create an account in this server's database. Once the account is made, it can be used to log in. Logging in can be done from the /login command or the settings page (in /settings).");
						return;
					}
					if (args.length < 3) {
						print("Usage: ", ERROR_COLOR);
						println("/" + name + " " + subcommand + " (username) (password)", ERROR_COLOR);
						return;
					}
					try {
						Authentication.getAuthServer().addUser(args[1], args[2]);
						println("Successfully added " + args[1], SUCCESS_COLOR);
					} catch (final Exception e) {
						println("An error occurred while trying to add " + args[1], ERROR_COLOR);
						return;
					}
				} else if (subcommand.equalsIgnoreCase("save")) {
					if (args.length < 2) {
						print("Usage: ", ERROR_COLOR);
						println("/" + name + " " + subcommand + " (file-path.extension)", ERROR_COLOR);
						print("Do ", INFO_COLOR);
						print("/" + name + " " + subcommand + " help ", Color.ORANGERED);
						println("for more details.", INFO_COLOR);
						return;
					}
					if (equalsHelp(args[1])) {
						printHelp("/" + name + " " + subcommand + " (file-path.extension)",
								"Saves the user accounts stored in this authentication server to a file. The accounts can then be loaded back up after the program has restarted, or they can be transported to a different computer and used there, etc. This will overwrite any data in the specified file with the auth server accounts.");
						return;
					}
					final File file = new File(args[1]);
					if (file.exists())
						file.delete();
					try {
						Authentication.getAuthServer().store(file);
					} catch (final IOException e) {
						println("Failed to create the file: " + file.getAbsolutePath() + "."
								+ (file.exists() ? " Note that this is probably NOT because the file exists." : ""),
								ERROR_COLOR);
						e.printStackTrace();
						return;
					} catch (final Exception e) {
						println("Failed to print data to the file: " + file.getAbsolutePath(), ERROR_COLOR);
						return;
					}
					println("Successfully printed the user data to the file, " + file.getAbsolutePath(), SUCCESS_COLOR);
				} else if (subcommand.equalsIgnoreCase("load")) {
					if (args.length < 2) {
						print("Usage: ", ERROR_COLOR);
						println("/" + name + " " + subcommand + " (file-path.extension)", ERROR_COLOR);
						print("Do ", INFO_COLOR);
						print("/" + name + " " + subcommand + " help ", Color.ORANGERED);
						println("for more details.", INFO_COLOR);
						return;
					}
					if (equalsHelp(args[1])) {
						printHelp("/" + name + " " + subcommand + " (file-path.extension)",
								"Loads the user accounts stored inside a saved file. NOTE that this will overwrite ALL the accounts currently stored in the auth server. If the file has one account and the auth server has 50 accounts, running this command will clear all 50 accounts in the auth server and replace them with the one loaded account. This account will not modify the file it reads in any way.");
						return;
					}

					File file = new File(args[1]);
					println("Attempting to locate file...", INFO_COLOR);
					if (!file.exists()) {
						println("The specified file does not exist...", ERROR_COLOR);
						return;
					}
					println("The file was found!", SUCCESS_COLOR);
					try {
						Authentication.getAuthServer().load(file);
						println("The server successfully loaded the file.", SUCCESS_COLOR);
					} catch (FileNotFoundException e) {
						println("The authentication server says that it couldn't find the file...", ERROR_COLOR);
						e.printStackTrace();
					} catch (UserDataParseException e) {
						println("The authentication server failed to parse the text in the file. An error message is as follows...",
								ERROR_COLOR);
						Logger logger = new Logger("Loader", Authentication.AUTH_SERVER_LOGGER);
						logger.log(e.getMessage());
						logger.logBold("Error occurred on line: " + e.line);

						e.printStackTrace();
					}

				} else if (equalsAnyIgnoreCase(subcommand, "print-users", "print-names", "printusers", "printnames",
						"pusers", "pnames", "pu", "pn")) {
					Collection<User> users = Authentication.getAuthServer().getUsers();
					if (users.isEmpty()) {
						println("There are no user accounts in the authentication server; there is nothing to print.",
								INFO_COLOR);
						return;
					}
					int numb = 1;
					for (User u : users) {
						print(numb + ". ", INFO_COLOR);
						println(u.username, SUCCESS_COLOR);
					}
				}
			}
		}

		@Override
		protected boolean match(final String name) {
			// Not ignorecase
			return equalsAny(name, "auth-server", "authserver", "#as");
		}
	};

	public static final Command CREATE_NEW_ACCOUNT = new ChatRoomCommand() {

		private String username, password;

		private final Runnable createAccount = new Runnable() {

			@Override
			public void run() {
				if (Platform.isFxApplicationThread()) {
					new Thread(this).start();
					return;
				}
				try {
					ChatRoom.INSTANCE.login(new Account(username,
							Authentication.getDefaultAuthenticationMethod().createNewAccount(username, password)));
					println("Successfully created a new account and logged you in to it!", SUCCESS_COLOR);
				} catch (TimeoutException e) {
					println("Connected to the server, but a timeout occurred. Your account was not created.",
							ERROR_COLOR);
				} catch (UsernameTakenException e) {
					println("That username is already taken! Please try again with another one.", ERROR_COLOR);
				} catch (InvalidUsernameException e) {
					println("That username is not allowed.", ERROR_COLOR);
				} catch (UnknownAuthenticationException e) {
					println("An unknown error occurred.", ERROR_COLOR);
					e.printStackTrace();
				} catch (AccountCreationDeniedException e) {
					println("This server currently does not allow account creation.", ERROR_COLOR);
					e.printStackTrace();
				} catch (ConnectException e) {
					println("The server could not be connected to. It might be down.", ERROR_COLOR);
				} catch (IOException e) {
					println("An error occurred while trying to contact the server.", ERROR_COLOR);
					e.printStackTrace();
				}
				username = password = null;
			}
		};

		private final CommandConsumer consumer = new CommandConsumer() {

			@Override
			public void consume(String command, String... args) {
				if (command.isEmpty()) {
					println("You must enter a " + username == null ? "password" : "username" + "to continue",
							ERROR_COLOR);
					addConsumer(this);
					return;
				} else if (username == null) {
					username = command;
					println("Username: " + username, SUCCESS_COLOR);
					if (args.length > 1) {
						password = args[1];
						println("Password: " + password, SUCCESS_COLOR);
						return;
					} else {
						println("Enter a password to continue:", INFO_COLOR);
						addConsumer(this);
						return;
					}
				} else if (password == null) {
					password = command;
					println("Password: " + password, SUCCESS_COLOR);
					createAccount.run();
					return;
				}
			}
		};

		@Override
		protected boolean match(String name) {
			return equalsAnyIgnoreCase(name, "create-account", "new-account", "create-new-account", "cna");
		}

		@Override
		protected void act(String name, String... args) {

			if (args.length > 0 && equalsHelp(args[0])) {
				printHelp("/" + name + " [username [password]]",
						"Creates an account using the given username and password. If a password is not provided but a username is, the password will be prompted for. If neither a username nor a password are provided, they will both be prompted for. If a password is given, but a username isn't, it will be treated as the username, and a password will be prompted for.",
						"This command won't work if you are logged into an account already.",
						(Authentication.isAuthServerRunning() ? "Since" : "If")
								+ " you are already running an authentication server, you should use the \"add user\" command (/auth-server add (username) (password)) to create an account. It is easier and can't result in connection errors. You can then log in to your account with the /login command.");
				return;
			}

			if (ChatRoom.INSTANCE.isLoggedIn()) {
				println("You are already logged in to an account. You can't create a new one.", ERROR_COLOR);
				return;
			}

			if (args.length > 0) {
				username = args[0];
				println("Username: " + username, SUCCESS_COLOR);
				if (args.length > 1) {
					password = args[1];
					println("Password: " + password, SUCCESS_COLOR);
					createAccount.run();
					return;
				} else {
					println("Enter a password to continue.", INFO_COLOR);
					addConsumer(consumer);
				}
			} else {
				addConsumer(consumer);
				println("Enter a username to continue.", INFO_COLOR);
			}
		}
	};

	public static final Command LOGOUT = new ChatRoomCommand() {

		@Override
		protected boolean match(String name) {
			return equalsAnyIgnoreCase(name, "logout", "log-out");
		}

		@Override
		protected void act(String name, String... args) {
			if (args.length > 0) {
				if (equalsHelp(args[0])) {
					printHelp("/" + name,
							"This command logs you out of an account if you are logged in to one. You may have to reconnect to some servers for the change to be visible on them.");
					return;
				}
				println("This command does not take any arguments...", ERROR_COLOR);
				return;
			}

			if (!ChatRoom.INSTANCE.isLoggedIn()) {
				println("You aren't logged in.", INFO_COLOR);
				return;
			}

			try {
				// The following isn't really necessary...
				Authentication.getDefaultAuthenticationMethod().logout(ChatRoom.INSTANCE.getAccount().username,
						ChatRoom.INSTANCE.getAccount().sessionID);
				println("Successfully logged you out of the authentication server.", SUCCESS_COLOR);
			} catch (TimeoutException e) {
				println("Connecting to the server and attempting to log you out timed out.", ERROR_COLOR);
			} catch (UsernameNotFoundException e) {
				println("The server could not find the username that you were logged in with!", ERROR_COLOR);
				e.printStackTrace();
			} catch (UnknownAuthenticationException e) {
				println("An unknown error occurred while trying to log you out.", ERROR_COLOR);
				e.printStackTrace();
			} catch (InvalidSessionIDException e) {
				println("Your sessionID had expired; you weren't originally logged in.", ERROR_COLOR);
				e.printStackTrace();
			} catch (IOException e) {
				println("An exception occurred while trying to connect to the server.", ERROR_COLOR);
				e.printStackTrace();
			} finally {
				ChatRoom.INSTANCE.logout();
				println("Successfully logged you out locally.", SUCCESS_COLOR);
			}

		}
	};
	public static final Command LOGIN = new ChatRoomCommand() {

		private String accountName, password;
		private final CommandConsumer usernameConsumer = new CommandConsumer() {

			@Override
			public void consume(final String command, final String... args) {
				if (command.equals("/cancel") && args.length == 0) {
					println("Cancelling login.", SUCCESS_COLOR);
					return;
				}

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
					println("Username: " + accountName, SUCCESS_COLOR);
					if (args.length > 0) {
						password = args[0];
						println("Password: " + password, SUCCESS_COLOR);
					} else {
						println("Please enter a password:", INFO_COLOR);
						addConsumer(this);
					}
				} else if (password == null) {
					password = command;
					println("Password: " + password, SUCCESS_COLOR);
					login.run();
					return;
				}

			}
		};

		private final Runnable login = () -> {
			Authentication.login(accountName, password);
			accountName = password = null;
		};

		@Override
		protected void act(final String name, final String... args) {
			if (args.length > 0) {
				if (args.length == 1 && equalsHelp(args[0])) {
					printHelp("/" + name + " [username [password]]",
							"Attempts to log you into your account, given your username and password. If a password is provided, a username is required before it for this command to work correctly. Otherwise, missing arguments will be prompted for.");
					return;
				}
				accountName = args[0];
				println("Username: " + accountName, SUCCESS_COLOR);
				if (args.length > 1) {
					password = args[1];
					println("Password: " + password, SUCCESS_COLOR);
					login.run();
					return;
				} else {
					println("Please enter a password:", INFO_COLOR);
					addConsumer(usernameConsumer);
				}
			} else {
				println("Please enter a username:", INFO_COLOR);
				addConsumer(usernameConsumer);
			}
		}

		@Override
		protected boolean match(final String name) {
			return name.equalsIgnoreCase("login");
		}
	};

	public static final Command SETTINGS = new ChatRoomCommand() {

		@Override
		protected void act(final String name, final String... args) {
			if (args.length > 0) {
				if (equalsHelp(args[0])) {
					printHelp("/" + name,
							"Opens up the settings window. This allows you to customize program settings and/or login.");
					println("Would you like to open the settings window? (Y/N)", INFO_COLOR);
					addConsumer(new CommandConsumer() {

						@Override
						public void consume(final String command, final String... args) {
							if (equalsAnyIgnoreCase(command, "yes", "y")) {
								println("Opening settings window...", SUCCESS_COLOR);
								// Try block is *currently* useless
								try {
									openSettingsWindow();
								} catch (final Exception e) {
									e.printStackTrace();
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
				try {
					openSettingsWindow();
				} catch (final Exception e) {
					e.printStackTrace();
					println("Failed to open window...", ERROR_COLOR);
				}
			}
		}

		@Override
		protected boolean match(final String name) {
			return name.equalsIgnoreCase("settings");
		}
	};

	public static final Command HOST_CALL = new ChatRoomCommand() {

		@Override
		protected void act(final String name, final String... args) {

			if (ChatRoom.INSTANCE.getCallServer() != null) {
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
					if (ChatRoom.INSTANCE.getCallServer() != null) {
						try {
							ChatRoom.INSTANCE.getCallServer().stop();
						} catch (final IOException e) {
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
			} catch (final IOException e) {
				println("An error occurred while trying to create a server.", ERROR_COLOR);
				e.printStackTrace();
				return;
			} catch (final NumberFormatException e) {
				println("The port you entered could not be parsed as a number.", ERROR_COLOR);
				return;
			}

			println("Successfully started hosting a call.", SUCCESS_COLOR);
		}

		@Override
		protected boolean match(final String name) {
			return equalsAnyIgnoreCase(name, "host-call", "hostcall");
		}

	};

	public static final Command CALL = new ChatRoomCommand() {

		@Override
		protected void act(final String name, final String... args) {
			if (args.length == 0) {
				print("You must pass an argument to this command. E.g., ", ERROR_COLOR);
				print("/" + name + " dusttoash.org", Color.ORANGERED);
				println(".", ERROR_COLOR);
				return;
			}

			if (args[0].equalsIgnoreCase("disconnect")) {
				try {
					if (ChatRoom.INSTANCE.getCallClient() == null)
						print("There is no active call for you to disconnect...", ERROR_COLOR);
					ChatRoom.INSTANCE.getCallClient().disconnect();
					ChatRoom.INSTANCE.setCallClient(null);
				} catch (final IOException e) {
					e.printStackTrace();
					println("A data streaming error occurred while trying to disconnect the client.", ERROR_COLOR);
				}
				return;
			}

			if (ChatRoom.INSTANCE.getCallClient() != null) {
				print("There is already a call active. Do ", ERROR_COLOR);
				print("/" + name + " disconnect ", Color.ORANGE);
				println("to disconnect from the current call.", ERROR_COLOR);
				println("(Note that you might not be in a call, but if you created one before and haven't cleared it yet, you can't create a new one, regardless of whether or not the previous call failed.)",
						WARNING_COLOR);
				return;
			}

			if (equalsHelp(args[0])) {
				printHelp("/" + name + " (server-address) [audio-level]",
						"Calls a callserver with the specified address.",
						"The (server-address) is the internet url or ip that is used to connect to the server.",
						"The [audio-level] is not a required parameter, but can be given. The [audio-level] allows you to specify the quality of the audio being sent to and from the server.",
						"I'm not too sure how this program works, but I don't think that anyone else will be able to hear you if they have different audio levels and they try to join the same call. Having different people in the same call with different audio levels may make some of them have to restart the program (or something). Again, not too big of a deal, but...",
						"There are 6 preset audio levels which can be selected by passing \"l\" (lowercase L, without quotes) and then a number from 1-6. Examples:",
						"\"l2\"", "\"l1\"", "\"l6\"",
						"You can also directly specify the audio level by simply entering a number without an \"l\" infront of it.",
						"The audio level is the sample rate of the sound data streamed to others in the call. The higher the sample rate, the better the audio quality.");
				return;
			}

			final String location = equalsAnyIgnoreCase(args[0], "self", "s") ? "localhost" : args[0];
			float sampleRate = DEFAULT_CALL_SAMPLE_RATE;

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
							break;
						case 3:
							sampleRate = 24000;
							break;
						case 4:
							sampleRate = 48000;
							break;
						case 5:
							sampleRate = 96000;
							break;
						case 6:
							sampleRate = 192000;
							break;
						default:
							sampleRate = DEFAULT_CALL_SAMPLE_RATE;
							break;
						}
						println("Audio level (AKA sample rate) set to " + sampleRate + ".", SUCCESS_COLOR);
					} catch (final NumberFormatException e) {
						println("Could not parse an audio level preset...", ERROR_COLOR);
						println("Usage: /" + name + " " + args[0] + " l[level]", Color.ORANGE);
						return;
					}
				} else
					try {
						sampleRate = Float.parseFloat(rate);
						println("Audio level (AKA sample rate) set to " + sampleRate + ".", SUCCESS_COLOR);
					} catch (final NumberFormatException e) {
						println("Could not parse an audio level...", ERROR_COLOR);
						println("Usage: /" + name + " " + args[0] + " [number]", Color.ORANGE);
						return;
					}
			}

			final float sampleRateResult = sampleRate;

			new Thread(new Runnable() {

				@Override
				public void run() {
					print("Calling ", INFO_COLOR);
					print(location, Color.CYAN);
					print(" at the sample rate ", INFO_COLOR);
					print("" + sampleRateResult, Color.CYAN);
					println(".", INFO_COLOR);
					try {
						ChatRoom.INSTANCE.setCallClient(new CallClient(location, DEFAULT_CALL_PORT,
								new AudioFormat(sampleRateResult, 16, 1, true, true)));
					} catch (ConnectException e) {
						println("The call server could not be connected to. It seems that there is not a server running on the specified address.",
								ERROR_COLOR);
					} catch (final LineUnavailableException e) {
						println("Failed to make the call client. Your microphone could not be accessed...",
								ERROR_COLOR);
						e.printStackTrace();
					} catch (final UnknownHostException e) {
						println("Failed to connect to the server. The server could not be found (i.e. its address could not be determined).",
								ERROR_COLOR);
					} catch (final IOException e) {
						println("Failed to connect to the server due to some data streaming error.", ERROR_COLOR);
						e.printStackTrace();
					}

				}
			}, "CALL-STARTER-THREAD").start();

		}

		@Override
		protected boolean match(final String name) {
			return name.equalsIgnoreCase("call");
		}
	};

	// THIS IS A REGULAR COMMAND OBJECT; it must be added to the command manager
	// manually.
	public static final Command _ESCAPE = new Command() {

		// Add this to the manager
		{
			ChatRoom.INSTANCE.commandManager.addCommand(this);
		}

		@Override
		protected void act(final String name, final String... args) {
			String text = name.substring(1);
			for (final String s : args)
				text += " " + s;
			sendText(text);
		}

		@Override
		protected boolean match(final String name) {
			return name.startsWith("/");
		}
	};

	public static final Command ESCAPE = new ChatRoomCommand() {

		@Override
		protected void act(final String name, final String... args) {
			String text = "";
			for (final String s : args)
				text += s + " ";
			sendText(text);

		}

		@Override
		protected boolean match(final String name) {
			return name.equalsIgnoreCase("escape");
		}
	};

	public static final Command CHANGELOG = new ChatRoomCommand() {

		@Override
		protected void act(final String name, final String... args) {

			if (args.length == 0) {
				final ChangelogParser parser = new ChangelogParser("/changelog.txt");
				parser.printChangelog(printer);

			} else {
				if (args.length > 1)
					println("Excessive args. Using only what is needed.", WARNING_COLOR);
				final String arg = args[0];
				if (equalsHelp(arg))
					// TODO Change this code when moving to better versioning.
					printHelp("/" + name + " [version-number]",
							"Prints the changelog for the current version of the program (if no arguments are provided in the command), or the changelog of a specific version of this program (if an argument is provided and a matching version is found on the program's website).",
							"As of right now, versions are simply numbers, starting at one and going up. Later, versions may have more normal names, such as v0.1.7.2 or something. (The versioning format with periods is quite ubiquitous as of now.)");
				else {
					int ver;
					try {
						// TODO Change this code when moving to better versioning.
						ver = Integer.parseInt(arg);
						final URL location = new URL(
								"http://dusttoash.org/chat-room/changelogs/changelog-" + ver + ".txt");

						final ChangelogParser parser = new ChangelogParser(location.openStream());
						print("Version: ", Color.MEDIUMAQUAMARINE);
						parser.printChangelog(printer);

					} catch (final NumberFormatException e) {
						println("Failed to parse your argument, " + arg + " as a number.", ERROR_COLOR);
						return;
					} catch (final MalformedURLException e) {
						println("Something went wrong when parsing the URL that was made to try and get data on the version you specified. This isn't a connection error.",
								ERROR_COLOR);
						e.printStackTrace();
						return;
					} catch (final IOException e) {
						println("Failed to get the version data from the remote server.", ERROR_COLOR);
						e.printStackTrace();
					}

				}
			}
		}

		@Override
		protected boolean match(final String name) {
			return name.equalsIgnoreCase("changelog");
		}
	};

	public static final Command UPDATE = new ChatRoomCommand() {

		@Override
		protected void act(final String name, final String... args) {
			// Update the program

			if (args.length > 0) {

				final String argument = args[0];
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
					final Reader versionInput = new InputStreamReader(
							new URL("http://dusttoash.org/chat-room/version").openStream());

					// This parses things backwards...
					int n;
					int inc = 0;

					String rawInput = "";
					while ((n = versionInput.read()) != -1)
						if (Character.isDigit(n))
							rawInput += (char) n;
					final char[] charArray = rawInput.toCharArray();
					for (int i = charArray.length - 1; i > -1; i--)
						latest += Math.pow(10, inc++) * Integer.parseInt("" + charArray[i]);

					lateSuccess = true;

					print("The latest available version is ", SUCCESS_COLOR);
					print("" + latest, Color.WHITE);
					println(".", SUCCESS_COLOR);
					println();

				} catch (final IOException e) {
					println("An error occurred while trying to connect to the download server. The latest version could not be determined.",
							ERROR_COLOR);
				}

				println("Attempting to determine the version that you have.", INFO_COLOR);
				try {
					final Reader versionInput = new InputStreamReader(getClass().getResourceAsStream("/version"));
					int n;
					int inc = 0;
					String rawInput = "";
					while ((n = versionInput.read()) != -1)
						if (Character.isDigit(n))
							rawInput += (char) n;
					final char[] charArray = rawInput.toCharArray();
					for (int i = charArray.length - 1; i > -1; i--)
						current += Math.pow(10, inc++) * Integer.parseInt("" + charArray[i]);

					currSuccess = true;

					print("You have version ", SUCCESS_COLOR);
					print("" + current, Color.WHITE);
					println(".", SUCCESS_COLOR);

				} catch (final NullPointerException e) {
					println("The version of your copy of this application could not be determined.", ERROR_COLOR);
				} catch (final IOException e) {
					println("There was an error while reading some data inside the app. Your local version could not be determined.",
							ERROR_COLOR);
				}

				if (currSuccess && lateSuccess)
					// Need update
					if (latest > current) {
						print("There is a newer version of ", Color.ORANGE);
						print("Chat Room ", Color.ORANGERED);
						println("available.", Color.ORANGE);

						final SimpleText text = new SimpleText();

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
					if (latest == current)
						println("You have the latest version. :D", SUCCESS_COLOR);
					else
						println("Your version is above the latest publicly released version. Congrats...?", INFO_COLOR);

			}
		}

		@Override
		protected boolean match(final String name) {
			return name.equalsIgnoreCase("update");
		}
	};

	public static final Command SET_NAME = new ChatRoomCommand() {

		final class SpecialConsoleText extends ConsoleText {

			public String text;

			public SpecialConsoleText(final String text) {
				this.text = text;
			}

			@Override
			public void print(final Console console) {

				console.printAll(println(), println(), println());
				for (final char c : text.toCharArray()) {
					// The text
					final Text t = new Text("" + c);

					// Default formatting
					formatText(t);

					// Assing some stuff
					t.setFill(new Color(Math.random(), Math.random(), Math.random(), 1));
					t.setFont(Font.font(t.getFont().getFamily(), Math.random() * 10 + 35)); // 35
																							// ~
																							// 45

					// Add a dropshadow
					final DropShadow ds = new DropShadow();
					ds.setColor(new Color(Math.random(), Math.random(), Math.random(), 1));
					t.setEffect(ds);

					// Add it to the console.
					console.printText(t);
				}
				console.printAll(println(), println(), println());

			}

		}

		@Override
		protected void act(final String name, final String... args) {
			if (args.length == 0) {
				println("I can't set your name unless you tell me what you want it to be... >:(", ERROR_COLOR);
				print("Usage: ", ERROR_COLOR);
				println("/set-name (name)", Color.ORANGE);
			} else {

				final String arg = args[0];
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
				final String username = args[0];
				ChatRoom.INSTANCE.setUsername(username);
				print("Your name was changed to ", INFO_COLOR);
				println(username, Color.CHARTREUSE);
			}
		}

		@Override
		protected boolean match(final String name) {
			return equalsAnyIgnoreCase(name, "setname", "set-name") || name.equals("sn");
		}
	};

	public static final Command SERVER = new ChatRoomCommand() {

		@Override
		protected void act(final String name, final String... args) {
			// "/server" - When the user enters this, we want to print information about the
			// server they are hosting or tell them that they don't have one running.
			if (args.length == 0) {
				print("Too few arguments. Usage: ", ERROR_COLOR);
				println("/" + name + " (subcommand)", Color.ORANGE);
				return;
			}

			// Cache the subcommand
			final String subcommand = args[0];

			// "/server help" - Print help for the /server command
			if (equalsHelp(subcommand))
				printHelp("/" + name + " (subcommand)",
						"Allows you to manage " + (ChatRoom.INSTANCE.isServerOpen() ? "your" : "a") + " server.");
			else
			// "/server stop" - Stop the server if it's running. If it isn't, tell the user.
			// (Unless they entered "/server stop help". In this case, we'd print help for
			// the stop subcmd.)
			if (equalsAnyIgnoreCase(subcommand, "stop", "end", "end-connection", "close", "disconnect"))

				// Handle a help request.
				if (args.length > 1 && equalsHelp(args[1]))
					printHelp("/" + name + " " + subcommand, "Stops your server, if you have one running.");
				else
				// Handle no server to close.
				if (!ChatRoom.INSTANCE.isServerOpen())
					println("You don't have a server open...", ERROR_COLOR);
				else
					try {
						ChatRoom.INSTANCE.getServer().stop();
						println("Your server was stopped successfully. ", SUCCESS_COLOR);
					} catch (IOException e) {
						e.printStackTrace();
						println("An error occurred while trying to close the server.", ERROR_COLOR);
					}
			else if (equalsAnyIgnoreCase(subcommand, "start", "make", "new", "connect", "m", "s", "n", "c")) {

				if (ChatRoom.INSTANCE.isServerOpen()) {
					println("You already have a server open and, thus, cannot create a new one.", ERROR_COLOR);
					return;
				}

				int port;
				if (args.length > 0)
					try {
						port = Integer.parseInt(args[0]);
					} catch (NumberFormatException e) {
						println("The port you entered could not be parsed as a number.", ERROR_COLOR);
						return;
					}
				else
					port = ChatRoom.DEFAULT_CHAT_PORT;

				try {
					// This throws a runtime exception if there is already a server running, but we
					// check for that above so we don't need to catch it here.
					ChatRoom.INSTANCE.startServer(port);
				} catch (IOException e) {
					e.printStackTrace();
					println("Failed to open the server.", ERROR_COLOR);
				}

			}

		}

		@Override
		protected boolean match(final String name) {
			return name.equalsIgnoreCase("server") || name.equals("s");
		}
	};

	public static final Command CONNECT = new ChatRoomCommand() {

		@Override
		protected boolean match(String name) {
			return name.equalsIgnoreCase("connect");
		}

		@Override
		protected void act(String name, String... args) {
			if (args.length == 0) {
				print("Please append a subcommand or type ", ERROR_COLOR);
				print("/" + name + " help", Color.WHITE);
				println(" for more help.", ERROR_COLOR);
			} else if (equalsHelp(args[0])) {
				printHelp("/" + name + " (address) [port]", "Allows you to connect to a server.");
			} else {
				String address = args[0];
				int port;
				try {
					port = args.length == 1 ? ChatRoom.DEFAULT_CHAT_PORT : Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					println("Could not parse your port into a number.", ERROR_COLOR);
					return;
				}

				if (ChatRoom.INSTANCE.isClientOpen()) {
					print("You are already connected to a server. Use the ", ERROR_COLOR);
					print("/client start", Color.WHITE);
					println(" command to disconnect and connect to a new one.", ERROR_COLOR);
				}
				try {
					ChatRoom.INSTANCE.createNewClient(address, port);
					println("Successfully connected to the server!", SUCCESS_COLOR);
				} catch (UnknownHostException e) {
					println("A server could not be located on the specified address. Did you type it correctly?",
							ERROR_COLOR);
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					print("The port you gave is a number, but it is not in the correct range. Please enter a port ",
							ERROR_COLOR);
					print("between", Color.FIREBRICK);
					println(" 0 and 65536.", ERROR_COLOR);
				} catch (IOException e) {
					println("A connection error occurred while trying to communicate with the server.", ERROR_COLOR);
					e.printStackTrace();
				}

			}
		}
	};

	public static final Command DISCONNECT = new ChatRoomCommand() {

		@Override
		protected boolean match(String name) {
			return name.equalsIgnoreCase("disconnect");
		}

		@Override
		protected void act(String name, String... args) {
			if (args.length > 0)
				if (equalsHelp(args[0]))
					printHelp("/" + name, "Disconnects you from a server, if you are currently connected to one.");
				else
					println("That subcommand was not found.", ERROR_COLOR);
			else if (ChatRoom.INSTANCE.isClientOpen()) {
				ChatRoom.INSTANCE.getClient().closeConnection();
				println("Successfully disconnected from the server.", SUCCESS_COLOR);
			} else
				println("You are not connected to a server.", ERROR_COLOR);

		}
	};

	public static final Command CLIENT = new ChatRoomCommand() {

		@Override
		protected void act(final String name, final String... args) {
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

				else {
					if (ChatRoom.INSTANCE.isClientOpen()) {
						ChatRoom.INSTANCE.getClient().closeConnection();
						println("Successfully closed the connection.", SUCCESS_COLOR);
					} else
						println("There is no open client for you to close...", ERROR_COLOR);
				}

			} else if (equalsHelp(subcommand)) {
				printHelp("/" + name + " (subcommand)", "Allows you to see information about or manipulate clients.");
				print("Possible subcommands: ", SUCCESS_COLOR);
				print("stop", Color.WHITE);
				println(".", SUCCESS_COLOR);
			} else if (equalsAnyIgnoreCase(subcommand, "start", "make", "new", "connect", "m", "s", "n", "c")) {
				if (args.length < 2) {
					println("Please specify a server address (and a server port if needed).", ERROR_COLOR);
					print("Example: ", Color.WHITE);
					println("/" + name + " " + subcommand + " dusttoash.org 35560", Color.LIGHTCORAL);
				} else
					try {
						if (args.length == 2) {
							print("Connecting to ", INFO_COLOR);
							print(args[1], Color.WHITE);
							println(".", INFO_COLOR);

							ChatRoom.INSTANCE.createNewClient(args[1]);
							println("Successfully connected to the server.", SUCCESS_COLOR);
						} else if (args.length == 3) {
							print("Connecting to ", INFO_COLOR);
							print(args[1], Color.WHITE);
							print(" on the port ", INFO_COLOR);
							print(args[2], Color.WHITE);
							println(".", INFO_COLOR);

							ChatRoom.INSTANCE.createNewClient(args[1], Integer.parseInt(args[2]));
							println("Successfully connected to the server.", SUCCESS_COLOR);
						}
					} catch (UnknownHostException e) {
						e.printStackTrace();
						println("Could not locate the server... Did you type in the right address?", ERROR_COLOR);
					} catch (IOException e) {
						e.printStackTrace();
						println("Some sort of connection error occurred while trying to connect to the server.",
								ERROR_COLOR);
					} catch (NumberFormatException e) {
						print("The port you entered, (", ERROR_COLOR);
						print(args[2], Color.PURPLE);
						println("), could not be parsed as a numerical port. (Remember that it must be between 0 and 65536, not including either.)",
								ERROR_COLOR);
					} catch (IllegalArgumentException e) {
						println("The port you entered was out of range. It must be BETWEEN 0 and 65536, but not 0 or 65536.",
								ERROR_COLOR);
					}

			}

		}

		@Override
		protected boolean match(final String name) {
			return name.equalsIgnoreCase("client") || name.equals("c");
		}
	};

	public static final Command CLEAR_SCREEN = new ChatRoomCommand() {

		@Override
		protected void act(final String name, final String... args) {
			if (args.length > 0 && equalsHelp(args[0])) {
				printHelp("/" + name,
						"This simply clears all the items inside your console. Any text from commands or other users will be cleared.");
				return;
			}
			ChatRoom.INSTANCE.getGUI().flow.getChildren().clear();
		}

		@Override
		protected boolean match(final String name) {
			return equalsAnyIgnoreCase(name, "cls", "clear-screen", "clearscreen");
		}
	};

	public static final ChatRoomCommand STATISTICS = new ChatRoomCommand() {

		@Override
		protected void act(final String name, final String... args) {
			println("Not yet implemented.", ERROR_COLOR);
			// TODO Implement
		}

		@Override
		protected boolean match(final String name) {
			return equalsAnyIgnoreCase(name, "stats", "statistics");
		}
	};

	public static final ChatRoomCommand CLEANUP = new ChatRoomCommand() {

		@Override
		protected void act(final String name, final String... args) {

			final long ram = Runtime.getRuntime().totalMemory(), start = System.currentTimeMillis();
			System.gc();
			final long time = System.currentTimeMillis() - start,
					freedMemory = ram - Runtime.getRuntime().totalMemory();

			if (freedMemory < 0) {
				println("Garbage cleanup somehow created more garbage...", ERROR_COLOR);
				print("Cleanup took ", ERROR_COLOR);
				new BoldText("" + time, Color.FIREBRICK).print(console);
				print(" milliseconds and used ", ERROR_COLOR);
				new BoldText(-freedMemory + "", Color.ORANGERED).print(console);
				println(" bytes...", SUCCESS_COLOR);
				println();
				println("Maybe you should try running cleanup again?...", INFO_COLOR);
			} else if (freedMemory == 0) {
				println("Garbage collection found no garbage, so nothing was cleaned. (This either means that ChatRoom has no garbage to clean, or that the garbage could not be cleaned for some reason. Likely the former.)",
						SUCCESS_COLOR);
				print("The attempted cleanup took ", SUCCESS_COLOR);
				new BoldText("" + time, Color.FIREBRICK).print(console);
				print(" milliseconds and freed ", SUCCESS_COLOR);
				new BoldText("nothing", Color.ORANGERED).print(console);
				println(".", SUCCESS_COLOR);
			} else {
				println("Cleaned up garbage!", SUCCESS_COLOR);
				print("Cleanup took ", SUCCESS_COLOR);
				new BoldText("" + time, Color.FIREBRICK).print(console);
				print(" milliseconds and freed ", SUCCESS_COLOR);
				new BoldText(freedMemory + "", Color.ORANGERED).print(console);
				println(" bytes!", SUCCESS_COLOR);
			}

		}

		@Override
		protected boolean match(final String name) {
			return equalsAnyIgnoreCase(name, "clean", "cleanup", "clear-lag", "clearlag", "clean-up");
		}
	};

	/**
	 * This is actually, conveniently, used to load this class (and, thus, all of
	 * the commands) at startup.
	 *
	 * @return The time (after all static initialization has taken place).
	 */
	public static long getTime() {
		return System.currentTimeMillis();
	}

	private static void openSettingsWindow() {
		ChatRoom.INSTANCE.settingsInstance.get().show();
		ChatRoom.INSTANCE.settingsInstance.get().requestFocus();
	}

	private static void print(final String text, final Color color) {
		new BasicInfoText(text, color).print(console);
	}

	private static void println() {
		new Println(console);
	}

	private static void println(final String text, final Color color) {
		print(text, color);
		println();
	}

	public static void sendText(final String text) {
		ChatRoom.INSTANCE.sendText(text);
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

	private Commands() {
	}

}
