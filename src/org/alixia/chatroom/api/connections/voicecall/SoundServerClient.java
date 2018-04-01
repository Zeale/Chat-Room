package org.alixia.chatroom.api.connections.voicecall;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

class SoundServerClient {

	private Socket socket;
	private CallServer owner;

	private boolean run = true;

	private final Thread receiver = new Thread(() -> {
		while (run)
			try {
				final byte[] data = new byte[socket.getInputStream().available()];
				socket.getInputStream().read(data);
				owner.sendSound(data, SoundServerClient.this);
			} catch (final IOException e) {
				e.printStackTrace();
				try {
					Thread.sleep(10);
				} catch (final InterruptedException e1) {
					e1.printStackTrace();
				}
			}
	});

	{
		receiver.setDaemon(true);
	}

	public SoundServerClient(final Socket socket, final CallServer owner) throws IOException {

		this.socket = socket;
		this.owner = owner;
		receiver.start();

	}

	public void close() throws IOException {
		run = false;
		socket.close();
	}

	public Socket getSocket() {
		return socket;
	}

	/**
	 * @param data
	 *            The data to send.
	 * @throws SocketException
	 *             If the client, that was represented by this object, disconnected.
	 * @throws IOException
	 *             If an {@link IOException} occurs.
	 */
	public void sendData(final byte[] data) throws SocketException, IOException {
		// With a sysout, we can see that data.length is 0. Sound is still sent,
		// however...
		socket.getOutputStream().write(data);
		socket.getOutputStream().flush();
	}

}
