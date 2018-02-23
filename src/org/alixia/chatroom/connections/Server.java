package org.alixia.chatroom.connections;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

import org.alixia.chatroom.connections.messages.Message;
import org.alixia.chatroom.connections.messages.ReplyMessage;
import org.alixia.chatroom.connections.messages.client.UserMessage;
import org.alixia.chatroom.connections.messages.server.BasicServerMessage;

public class Server extends NamedObject {

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

	public Server(String name) throws IOException {
		super(name);
		socket = new ServerSocket(0);
		acceptThread.start();
	}

	public Server(final int port, String name) throws IOException {
		super(name);
		socket = new ServerSocket(port);
		acceptThread.start();
	}

	protected void acceptConnection(final Socket connection) {
		try {
			ServerClient client = new ServerClient(connection);
			client.setListener(new ConnectionListener() {

				@Override
				public void objectReceived(Serializable object) {
					if (object instanceof UserMessage) {
						sendAll((Message) object, client);
						try {
							client.sendObject(new ReplyMessage((Message) object));
						} catch (SocketException e) {
							connections.remove(client);
							client.getListener().connectionClosed();
						} catch (Exception e) {
						}
					}
				}

				@Override
				public void connectionClosed() {
					sendAll(new BasicServerMessage("Someone left the server!"), client);
				}
			});
			sendAll(new BasicServerMessage("A user has connected!"), client);
			connections.add(client);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void sendAll(Message message, ServerClient... excludedClients) {

		OUTER: for (int i = 0; i < connections.size(); i++) {
			ServerClient sc = connections.get(i);
			for (ServerClient sc0 : excludedClients)
				if (sc == sc0)
					continue OUTER;

			try {
				sc.sendObject(message);
			} catch (SocketException e) {
				connections.remove(sc);
				sc.getListener().connectionClosed();
				i--;
			}
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
