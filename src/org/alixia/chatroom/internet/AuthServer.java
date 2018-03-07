package org.alixia.chatroom.internet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
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

	public void addUser(String username, String password) {
		users.put(username, new User(username, password));
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
							sender.writeObject(new SessionIDPacket(
									users.get(((LoginRequestPacket) packet).username).makeID(), Success.SUCCESS));
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
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public final String username, password;
		private UUID sessionID;

		public User(String username, String password) {
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
