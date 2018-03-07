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

public class AuthServer {

	private Map<String, User> users = new HashMap<>();
	private final ServerSocket socket;

	private boolean run = true;

	public AuthServer(int port) throws IOException {
		socket = new ServerSocket(port);
		handler.start();
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

			// Catch a sendable exception below and send it.

			// First check if the connection is a server (who's verifying a session id), or
			// a client (who's trying to log in).

			Object packet = reader.readObject();

			if (packet instanceof LoginRequestPacket) {
				// We must check to see if the login information in the packet is correct.
				if (!users.containsKey(((LoginRequestPacket) packet).username)) {
					// User not registered. If they were, their username would be in the map (as a
					// key).

				}

			}

		} catch (Exception e) {

			e.printStackTrace();
			return;
		} catch (OutOfMemoryError e) {
			System.gc();
		}

	}

	{
		handler.setDaemon(true);
	}

	public static final class User implements Serializable {
		private final String username, password;
		private UUID sessionID;

		public User(String username, String password) {
			this.username = username;
			this.password = password;
		}

		public UUID makeID() {
			return sessionID = UUID.randomUUID();
		}

		public boolean IDsMatch(UUID id) {
			return sessionID.equals(id);
		}

	}

}
