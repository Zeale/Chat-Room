package org.alixia.chatroom.api.connections.voicecall;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.logging.Logger;

import javafx.scene.paint.Color;

public class CallServer {

	public static final Logger LOGGER = new Logger("CallServer", ChatRoom.LOGGER);
	static {
		LOGGER.bracketColor = Color.RED;
		LOGGER.separatorColor = LOGGER.parentColor = LOGGER.childColor = Color.AQUA;
	}

	private final ServerSocket socket;
	private boolean run = true;

	private final List<SoundServerClient> connections = new LinkedList<>();

	private final Runnable acceptImpl = new Runnable() {

		@Override
		public void run() {

			while (run)
				try {
					final Socket connection = socket.accept();

					LOGGER.log("Accepted the connection " + connection.getInetAddress());
					new Thread(() -> {
						try {
							handleConnection(connection);
						} catch (final IOException e) {
							e.printStackTrace();
						}
					}).start();
				} catch (final SocketException e) {
					return;
				} catch (final IOException e) {
					System.err.println("Failed to accept a connection to a voice server.");
					e.printStackTrace();
				} catch (final Exception e) {
					e.printStackTrace();
				}

			accepter = new Thread(this);
			accepter.setDaemon(true);

		}
	};

	private Thread accepter = new Thread(acceptImpl);

	{
		accepter.setDaemon(true);
	}

	public CallServer() throws IOException {
		this(0);
	}

	public CallServer(final int port) throws IOException {
		socket = new ServerSocket(port);
		accepter.start();
	}

	protected void handleConnection(final Socket connection) throws IOException {
		final SoundServerClient client = new SoundServerClient(connection, this);
		connections.add(client);
	}

	void sendSound(final byte[] data, final SoundServerClient sender) {
		sendToAll(data, sender);
	}

	private void sendToAll(final byte[] data, final SoundServerClient... exceptedClients) {
		DATA_LOOP: for (final SoundServerClient ssc : connections) {
			for (final SoundServerClient essc : exceptedClients)
				if (ssc == essc)
					continue DATA_LOOP;
			try {
				ssc.sendData(data);
			} catch (final SocketException e) {
				System.out.println(ssc);
				connections.remove(ssc);
			} catch (final Exception e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
		}
	}

	public void stop() throws IOException {
		run = false;
		for (final SoundServerClient ssc : connections)
			ssc.close();
	}

}
