package org.alixia.chatroom.internet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class AuthServer {

	private Map<String, String> users = new HashMap<>();
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

	}

	{
		handler.setDaemon(true);
	}

	public static final class User implements Serializable {
		private final String username, password;

		public User(String username, String password) {
			this.username = username;
			this.password = password;
		}
	}

}
