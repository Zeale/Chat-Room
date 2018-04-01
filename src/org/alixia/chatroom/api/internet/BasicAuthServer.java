package org.alixia.chatroom.api.internet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.UUID;

import org.alixia.chatroom.api.internet.CreateAccountReplyPacket.ErrType;
import org.alixia.chatroom.api.internet.LogoutReplyPacket.ErrorType;
import org.alixia.chatroom.api.internet.SessionIDPacket.Success;

public class BasicAuthServer {

	public static final class User implements Serializable {

		/**
		 * SUID
		 */
		private static final long serialVersionUID = 1L;

		private static boolean verify(final String text) {
			for (final char c : text.toCharArray())
				if (Character.isWhitespace(c))
					return false;
			return true;
		}

		public static boolean verifyPassword(final String password) {
			return verify(password);
		}

		public static boolean verifyUsername(final String username) {
			return verify(username);
		}

		public final String username, password;
		private UUID sessionID;

		public User(final String username, final String password) throws IllegalArgumentException {
			if (!(verifyUsername(username) && verifyPassword(password)))
				throw new IllegalArgumentException("Illegal username and/or password.");
			this.username = username;
			this.password = password;
		}

		public void clearID() {
			makeID();
		}

		public boolean IDsMatch(final UUID id) {
			return sessionID.equals(id);
		}

		public UUID makeID() {
			return sessionID = UUID.randomUUID();
		}

		public boolean passwordsMatch(final String password) {
			return password.equals(this.password);
		}

	}

	public static final class UserDataParseException extends Exception {
		/**
		 * SUID
		 */
		private static final long serialVersionUID = 1L;
		public final String line;

		public UserDataParseException(final String message, final String line) {
			super(message);
			this.line = line;
		}

	}

	public static Map<String, User> read(final File path) throws FileNotFoundException, UserDataParseException {

		final Map<String, User> users = new HashMap<>();

		try (Scanner scanner = new Scanner(path);) {
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				if (!line.contains(":"))
					throw new UserDataParseException(
							"No colon separator found in line (:). A colon is used to separate the username from the password of a user. Each line has a different user's data on it. Without a colon on a line, there's no telling of what part is the username or password.",
							line);
				final String username = line.substring(0, line.indexOf(":"));
				final String password = line.substring(line.indexOf(":") + 1);

				users.put(username, new User(username, password));

			}
		}
		return users;
	}

	public static Map<String, User> read(final String path) throws FileNotFoundException, UserDataParseException {
		return read(new File(path));
	}

	private Map<String, User> users = new HashMap<>();

	private final ServerSocket socket;

	private boolean run = true;

	private Thread handler = new Thread(new Runnable() {

		@Override
		public void run() {
			int errCount = 0;
			while (run)
				try {
					final Socket connection = socket.accept();
					new Thread(() -> handle(connection)).start();
					errCount = 0;
				} catch (final SocketException e) {
					run = false;
					break;
				} catch (final Throwable e) {
					if (errCount > 5) {
						System.err.println("CLOSING AUTHSERVER DUE TO REPETITIVE EXCEPTIONS...");
						break;
					}
					e.printStackTrace();
					System.out.println();
					System.err.println("CONTINUING");
					errCount++;

				}
			handler = new Thread(this);
			handler.setDaemon(true);
		}
	});

	{
		handler.setDaemon(true);
	}

	private boolean accountCreationEnabled = true;

	public BasicAuthServer(final int port) throws IOException {
		socket = new ServerSocket(port);
		handler.start();
	}

	public UUID addUser(final String username, final String password) {
		if (users.containsKey(username))
			return null;
		final User user = new User(username, password);
		users.put(username, user);

		return user.makeID();
	}

	public boolean canRun() {
		return !socket.isClosed();
	}

	public void close() {
		run = false;
	}

	public void dispose() throws IOException {
		close();
		socket.close();
	}

	public Collection<User> getUsers() {
		return users.values();
	}

	private void handle(final Socket connection) {

		try {

			// Make our communication objs.
			final ObjectInputStream reader = new ObjectInputStream(connection.getInputStream());
			final ObjectOutputStream sender = new ObjectOutputStream(connection.getOutputStream());

			// First check if the connection is a server (who's verifying a session id), or
			// a client (who's trying to log in).

			final Object packet = reader.readObject();// Blocking method
			try {
				// Issue a SessionID when they login. They can use this to communicate instead
				// of repeatedly sending over the password.
				//
				// The ID is given if their password is correct for the user name they give.
				// Obviously, the user name must also be in this server's database.
				if (packet instanceof LoginRequestPacket)
					// We must check to see if the login information in the packet is correct.
					if (users.containsKey(((LoginRequestPacket) packet).username))
						if (users.get(((LoginRequestPacket) packet).username)
								.passwordsMatch(((LoginRequestPacket) packet).password))
							sender.writeObject(new SessionIDPacket(users.get(((LoginRequestPacket) packet).username)
									// Only if the login is successful, will we make a new ID.
									.makeID(), Success.SUCCESS));
						else
							sender.writeObject(new SessionIDPacket(null, Success.WRONG_PASSWORD));
					else
						// User not registered. If they were, their user name would be in the map (as a
						// key).
						sender.writeObject(new SessionIDPacket(null, Success.USERNAME_NOT_FOUND));

				// Verify that a given SessionID is valid for the given user name.
				else if (packet instanceof VerificationRequestPacket)
					sender.writeObject(
							new VerificationPacket(users.containsKey(((VerificationRequestPacket) packet).username)
									&& users.get(((VerificationRequestPacket) packet).username)
											.IDsMatch(((VerificationRequestPacket) packet).sessionID)));
				else if (packet instanceof CreateAccountRequestPacket) {
					UUID sessid = null;
					ErrType err = null;
					if (isAccountCreationEnabled())
						if (isUsernameOpen(((CreateAccountRequestPacket) packet).name))
							if (isUsernameValid(((CreateAccountRequestPacket) packet).name))
								sessid = addUser(((CreateAccountRequestPacket) packet).name,
										((CreateAccountRequestPacket) packet).password);
							else
								err = ErrType.INVALID_USERNAME;
						else
							err = ErrType.USERNAME_EXISTS;
					else
						err = ErrType.REQUEST_DENIED;
					sender.writeObject(new CreateAccountReplyPacket(sessid, err));
				} else if (packet instanceof LogoutRequestPacket)
					sender.writeObject(new LogoutReplyPacket(users.containsKey(((LogoutRequestPacket) packet).username)
							? users.get(((LogoutRequestPacket) packet).username).IDsMatch(
									((LogoutRequestPacket) packet).sessionID) ? null : ErrorType.INVALID_SESSION_ID
							: ErrorType.USERNAME_NOT_FOUND));

				sender.flush();
			} finally {
				connection.close();
			}

		} catch (final Exception e) {
			e.printStackTrace();
			return;
		} catch (final OutOfMemoryError e) {
			System.gc();
			System.err.println("Out of memory error @");
			System.err.println("AuthServer.handle()");
		}

	}

	public boolean isAccountCreationEnabled() {
		return accountCreationEnabled;
	}

	public boolean isRunning() {
		return handler.isAlive();
	}

	public boolean isUsernameOpen(final String username) {
		return !isUserRegistered(username);
	}

	public boolean isUsernameValid(final String username) {
		for (final char c : username.toCharArray())
			if (Character.isWhitespace(c))
				return false;
		return username.length() > 2;
	}

	public boolean isUserRegistered(final String username) {
		return users.containsKey(username);
	}

	public void load(final File path) throws FileNotFoundException, UserDataParseException {
		users = read(path);
	}

	public void load(final String path) throws FileNotFoundException, UserDataParseException {
		load(new File(path));
	}

	public void setAccountCreationEnabled(final boolean accountCreationEnabled) {
		this.accountCreationEnabled = accountCreationEnabled;
	}

	public void start() {
		if (canRun() && !isRunning())
			handler.start();
	}

	public void store(final File path) throws FileNotFoundException, IOException {
		path.getParentFile().mkdirs();
		path.createNewFile();
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(path))) {
			for (final Entry<String, User> e : users.entrySet())
				// The first colon (:) will be parsed as a separator between usernames and
				// passwords when this file is read back into the program. The only char that we
				// can't allow in usernames or passwords is '\n'. That char will be used to
				// separate each user in the file.
				//
				// Since Windows uses \r\n isntead of \n for returns, if a person on Windows
				// opens the file and makes an edit, such as adding a username, I believe that
				// any return they add will be saved as \r\n instead of \n. Since both of these
				// look the same to the user, they won't see anything wrong. This program would
				// also read \r into the password. Because of this, no whitespace chars will be
				// allowed inside usernames or passwords.
				writer.println(e.getKey() + ":" + e.getValue().password);

			writer.flush();
		}

	}

	public void store(final String path) throws FileNotFoundException, IOException {
		store(new File(path));
	}

}
