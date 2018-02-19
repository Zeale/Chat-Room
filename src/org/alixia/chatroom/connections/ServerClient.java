package org.alixia.chatroom.connections;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

class ServerClient {

	private boolean connectionClosed;

	private final Socket socket;

	private final ObjectInputStream objIn;
	private final ObjectOutputStream objOut;

	private ConnectionListener listener;

	public void setListener(ConnectionListener listener) {
		this.listener = listener;
		if (!outputThread.isAlive())
			outputThread.start();
	}

	public ConnectionListener getListener() {
		return listener;
	}

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

	private void pause() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(final String message) throws IOException {
		sendObject(message);
	}

	public void sendObject(final Serializable object) throws RuntimeException {
		try {
			objOut.writeObject(object);
			objOut.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Resets the output stream. This should ditch any references to objects in the
	 * stream.
	 */
	public void reset() {
		try {
			objOut.reset();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
