package org.alixia.chatroom.internet;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.UUID;

import org.alixia.chatroom.internet.SessionIDPacket.Success;

public class AuthServer {

	private Map<String, User> users = new HashMap<>();
	private final ServerSocket socket;

	private boolean run = true;

	public AuthServer(int port) throws IOException {
		socket = new ServerSocket(port);
		handler.start();
	}

	public boolean addUser(String username, String password) {
		if (users.containsKey(username))
			return false;
		users.put(username, new User(username, password));
		return true;
	}

	public void store(File path) throws FileNotFoundException {
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(path))) {
			for (Entry<String, User> e : users.entrySet())
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
				writer.println(e.getKey() + ":" + e.getValue());

			writer.flush();
		}

	}

	public void store(String path) throws FileNotFoundException {
		store(new File(path));
	}

	public static Map<String, User> read(File path) throws FileNotFoundException, UserDataParseException {

		Map<String, User> users = new HashMap<>();

		try (Scanner scanner = new Scanner(path);) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (!line.contains(":"))
					throw new UserDataParseException(
							"No colon separator found in line (:). A colon is used to separate the username from the password of a user. Each line has a different user's data on it. Without a colon on a line, there's no telling of what part is the username or password.",
							line);
				String username = line.substring(0, line.indexOf(":") + 1);
				String password = line.substring(line.indexOf(":") + 1);

				users.put(username, new User(username, password));

			}
		}
		return users;
	}

	public void load(File path) throws FileNotFoundException, UserDataParseException {
		this.users = read(path);
	}

	public void load(String path) throws FileNotFoundException, UserDataParseException {
		load(new File(path));
	}

	public static final class UserDataParseException extends Exception {
		/**
		 * SUID
		 */
		private static final long serialVersionUID = 1L;
		public final String line;

		public UserDataParseException(String message, String line) {
			super(message);
			this.line = line;
		}

	}

	public static Map<String, User> read(String path) throws FileNotFoundException, UserDataParseException {
		return read(new File(path));
	}

	private Thread handler = new Thread(new Runnable() {

		@Override
		public void run() {
			int errCount = 0;
			while (run) {
				try {
					Socket connection = socket.accept();
					new Thread(() -> handle(connection)).start();
				} catch (Throwable e) {
					e.printStackTrace();
					System.out.println();
					System.err.println("CONTINUING");
					errCount++;
					if (errCount > 5) {
						System.err.println("CLOSING AUTHSERVER DUE TO REPETITIVE EXCEPTIONS...");
					}
				}
			}
			handler = new Thread(this);
			handler.setDaemon(true);
		}
	});

	private void handle(Socket connection) {

		try {

			// Make our communication objs.
			ObjectInputStream reader = new ObjectInputStream(connection.getInputStream());
			ObjectOutputStream sender = new ObjectOutputStream(connection.getOutputStream());

			// First check if the connection is a server (who's verifying a session id), or
			// a client (who's trying to log in).

			Object packet = reader.readObject();// Blocking method
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

				sender.flush();
			} finally {
				connection.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return;
		} catch (OutOfMemoryError e) {
			System.gc();
			System.err.println("Out of memory error @");
			System.err.println("AuthServer.handle()");

		}

	}

	{
		handler.setDaemon(true);
	}

	public static final class User implements Serializable {

		public static boolean verifyUsername(String username) {
			return verify(username);
		}

		public static boolean verifyPassword(String password) {
			return verify(password);
		}

		private static boolean verify(String text) {
			for (char c : text.toCharArray())
				if (Character.isWhitespace(c))
					return false;
			return true;
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public final String username, password;
		private UUID sessionID;

		public User(String username, String password) throws IllegalArgumentException {
			if (!(verifyUsername(username) && verifyPassword(password)))
				throw new IllegalArgumentException("Illegal username and/or password.");
			this.username = username;
			this.password = password;
		}

		public boolean passwordsMatch(String password) {
			return password.equals(this.password);
		}

		public UUID makeID() {
			return sessionID = UUID.randomUUID();
		}

		public boolean IDsMatch(UUID id) {
			return sessionID.equals(id);
		}

	}

}
