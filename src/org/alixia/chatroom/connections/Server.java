package org.alixia.chatroom.connections;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

public class Server {

	private final ServerSocket socket;

	private boolean accept = true, running = true;

	private Thread acceptThread = new Thread(new Runnable() {

		@Override
		public void run() {
			while (accept && running)
				try {
					final Socket connection = socket.accept();
					new Thread(() -> acceptConnection(connection)).start();
				} catch (final SocketException e) {
					// Server terminated
				} catch (final IOException e) {
					System.err.println("Failed to accept an incoming connection.");
				}
			acceptThread = new Thread(this);
		}
	});

	public Server() throws IOException {
		socket = new ServerSocket(0);
	}

	public Server(final int port) throws IOException {
		socket = new ServerSocket(port);
		acceptThread.start();
	}

	protected void acceptConnection(final Socket connection) {
		try {
			// Add a listener to the ServerClient.
			ServerClient client = new ServerClient(connection);
			connections.add(client);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void allowIncomingConnections() {
		final boolean accept = this.accept;
		this.accept = true;
		if (!accept)
			acceptThread.start();
	}

	public void blockIncomingConnections() {
		accept = false;

	}

	private List<ServerClient> connections = new LinkedList<>();

	public void stop() throws IOException {
		blockIncomingConnections();
		running = false;
		socket.close();
		for (final Object o : connections.toArray()) {
			final ServerClient c = (ServerClient) o;
			c.closeConnection();
		}
	}

}
