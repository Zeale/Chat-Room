package org.alixia.chatroom.connections;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

import org.alixia.chatroom.api.Account;
import org.alixia.chatroom.connections.messages.Message;
import org.alixia.chatroom.connections.messages.ReplyMessage;
import org.alixia.chatroom.connections.messages.client.BasicUserMessage;
import org.alixia.chatroom.connections.messages.client.requests.NameChangeRequest;
import org.alixia.chatroom.connections.messages.server.BasicServerMessage;
import org.alixia.chatroom.connections.messages.server.RelayedUserMessage;
import org.alixia.chatroom.internet.Authentication;
import org.alixia.chatroom.internet.authmethods.AuthenticationResult;
import org.alixia.chatroom.logging.Logger;

public class Server extends NamedObject {

	public static final Logger SERVER_LOGGER = new Logger("SERVER");

	private final ServerSocket socket;

	private boolean running = true;

	private Thread acceptThread = new Thread(new Runnable() {

		@Override
		public void run() {
			while (running)
				try {
					final Socket connection = socket.accept();
					new Thread(() -> acceptConnection(connection)).start();
				} catch (final SocketException e) {
					// Server terminated. Do not make acceptThread point to a new Thread obj. This
					// Server object is no longer usable since the socket has been closed.
					return;
				} catch (final IOException e) {
					System.err.println("Failed to accept an incoming connection.");
				}
			acceptThread = new Thread(this);
			acceptThread.setDaemon(true);
		}
	});
	{
		acceptThread.setDaemon(true);
	}

	private final List<ServerClient> connections = new LinkedList<>();

	public Server(final int port, final String name) throws IOException {
		super(name);
		socket = new ServerSocket(port);
		acceptThread.start();
	}

	public Server(final String name) throws IOException {
		super(name);
		socket = new ServerSocket(0);
		acceptThread.start();
	}

	/**
	 * This is the method that handles new connections.
	 *
	 * @param connection
	 *            The new connection.
	 */
	protected void acceptConnection(final Socket connection) {
		try {
			final ServerClient client = new ServerClient(connection);
			client.setListener(new ConnectionListener() {

				@Override
				public void connectionClosed() {
					sendAll(new BasicServerMessage("Someone left the server!"), client);
				}

				@Override
				public void objectReceived(final Serializable object) {

					if (object instanceof BasicUserMessage) {
						sendAll(new RelayedUserMessage(client.getUsername(), ((BasicUserMessage) object).text,
								client.isAnonymous() ? "Anonymous" : client.getAccountName()), client);
						try {
							client.sendObject(
									new RelayedUserMessage(client.getUsername(), ((BasicUserMessage) object).text,
											client.isAnonymous() ? "This is you." : client.getAccountName()));
						} catch (SocketException | RuntimeException e1) {
							e1.printStackTrace();
						}

						try {
							client.sendObject(new ReplyMessage((Message) object));
						} catch (final SocketException e) {
							connections.remove(client);
							client.getListener().connectionClosed();
						} catch (final Exception e) {
						}
					} else if (object instanceof Account) {
						final Account account = (Account) object;
						try {
							final AuthenticationResult auth = Authentication.getDefaultAuthenticationMethod()
									.authenticate(account.username, account.sessionID);
							if (auth.verified) {
								// Should only be set when logging in!
								client.setAccountName(account.username);
								client.setUsername(account.username);
								client.sendMessage("Successfully verified your login information.");
							} else
								client.sendMessage("Your login information was incorrect...");

						} catch (final IOException e) {
							e.printStackTrace();
							try {
								client.sendObject(new BasicServerMessage(
										"The server was unable to connect to its authentication server to verify your login information."));
							} catch (SocketException | RuntimeException e1) {
								e1.printStackTrace();
							}
						}

					} else if (object instanceof NameChangeRequest) {
						final String name = ((NameChangeRequest) object).newName;
						try {
							if (!isUsernameValid(name))
								client.sendMessage("That username is not valid!");
							else {
								client.sendMessage("Your name was set to " + name);
								client.setUsername(name);
							}
						} catch (final IOException e) {
							e.printStackTrace();
						}
					}

				}
			});
			sendAll(new BasicServerMessage("A user has connected!"), client);
			connections.add(client);

		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void blockIncomingConnections() {
		// The thread will notice that "running" is false and stop itself. We don't need
		// to stop it.
		running = false;
	}

	private boolean isUsernameValid(final String name) {
		for (final char c : name.toCharArray())
			if (Character.isWhitespace(c))
				return false;
		return name.length() > 3;
	}

	private void sendAll(final Message message, final ServerClient... excludedClients) {

		OUTER: for (int i = 0; i < connections.size(); i++) {
			final ServerClient sc = connections.get(i);
			for (final ServerClient sc0 : excludedClients)
				if (sc == sc0)
					continue OUTER;

			try {
				sc.sendObject(message);
			} catch (final SocketException e) {
				connections.remove(sc);
				sc.getListener().connectionClosed();
				i--;
			}
		}

	}

	/**
	 * Starts the thread that accepts incoming connections if it is not already
	 * running.
	 */
	public void startConnectionThread() {
		// "running" needs to be true for the thread to run.
		running = true;
		if (acceptThread.isAlive())
			try {
				acceptThread.start();
			} catch (final IllegalThreadStateException e) {
				// Do nothing. The thread is (probably) already running
			}
	}

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
