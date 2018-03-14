package org.alixia.chatroom.api.connections;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.alixia.chatroom.api.connections.messages.server.BasicServerMessage;

class ServerClient {

	public static final String DEFAULT_USERNAME = "Anonymous";

	private String username = DEFAULT_USERNAME, accountName;

	private boolean connectionClosed;

	private final Socket socket;
	private final ObjectInputStream objIn;
	private final ObjectOutputStream objOut;

	private ConnectionListener listener;

	private Thread outputThread = new Thread(new Runnable() {

		@Override
		public void run() {
			int socketExceptionCount = 0;
			while (!connectionClosed && listener != null) {

				Serializable obj;
				try {
					if (socket.isClosed())
						closeConnection();
					obj = (Serializable) objIn.readObject();
					if (listener != null)
						listener.objectReceived(obj);
					else
						break;

				} catch (final EOFException e) {
					closeConnection();
					return;
				} catch (final SocketException e) {
					socketExceptionCount++;
					pause();
					if (socketExceptionCount > 3)
						closeConnection();
				} catch (ClassNotFoundException | IOException e1) {
					e1.printStackTrace();
				}

				try {
					Thread.sleep(10);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}

			outputThread = new Thread(this);

			return;
		}
	});

	public ServerClient(final Socket socket) throws IOException {
		this.socket = socket;

		objOut = new ObjectOutputStream(socket.getOutputStream());
		objIn = new ObjectInputStream(socket.getInputStream());

	}

	public ServerClient(final String hostname, final int port) throws UnknownHostException, IOException {
		socket = new Socket(hostname, port);

		objOut = new ObjectOutputStream(socket.getOutputStream());
		objIn = new ObjectInputStream(socket.getInputStream());

	}

	public void closeConnection() {
		connectionClosed = true;
		try {
			socket.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public String getAccountName() {
		return accountName;
	}

	public ConnectionListener getListener() {
		return listener;
	}

	public String getUsername() {
		return username;
	}

	/**
	 * Returns whether or not this client has an account name set.
	 * 
	 * @return <code>true</code> if {@link #getAccountName()} will return null,
	 *         <code>false</code> otherwise.
	 */
	public boolean isAnonymous() {
		return getAccountName() == null;
	}

	/**
	 * Returns <code>true</code> if this client is logged in, <code>false</code>
	 * otherwise.
	 * 
	 * @return !{@link #isAnonymous()}.
	 */
	public boolean isLoggedIn() {
		return !isAnonymous();
	}

	private void pause() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Resets the output stream. This should ditch any references to objects in the
	 * stream.
	 */
	public void reset() {
		try {
			objOut.reset();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sends a text message to the client as the server.
	 *
	 * @param message
	 *            The message to send.
	 * @throws IOException
	 *             If an {@link IOException} occurs, as thrown by
	 *             {@link #sendObject(Serializable)}.
	 */
	public void sendMessage(final String message) throws IOException {
		sendObject(new BasicServerMessage(message));
	}

	public void sendObject(final Serializable object) throws SocketException, RuntimeException {
		try {
			objOut.writeObject(object);
			objOut.flush();
		} catch (final SocketException e) {
			throw e;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setAccountName(final String accountName) {
		this.accountName = accountName;
	}

	public void setListener(final ConnectionListener listener) {
		this.listener = listener;
		if (!outputThread.isAlive())
			outputThread.start();
	}

	private boolean usernameSet;

	public boolean hasSetUsername() {
		return usernameSet;
	}

	public void setUsername(final String username) {
		usernameSet = true;
		this.username = username;
	}

	/**
	 * Basically, logs this {@link ServerClient} out, from the perspective of the
	 * server.
	 * 
	 */
	public void clearLogin() {
		username = DEFAULT_USERNAME;
		accountName = null;
	}

}
