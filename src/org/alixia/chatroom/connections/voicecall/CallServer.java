package org.alixia.chatroom.connections.voicecall;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

public class CallServer {
	private final ServerSocket socket;
	private boolean run = true;

	private List<SoundServerClient> connections = new LinkedList<>();

	private final Runnable acceptImpl = new Runnable() {

		@Override
		public void run() {

			while (run)
				try {
					Socket connection = socket.accept();
					System.out.println("SOUNDSERVER: Accepted the connection " + connection.getInetAddress());
					new Thread(() -> {
						try {
							handleConnection(connection);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}).start();
				} catch (SocketException e) {
					return;
				} catch (IOException e) {
					System.err.println("Failed to accept a connection to a voice server.");
					e.printStackTrace();
				} catch (Exception e) {
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

	public CallServer(int port) throws IOException {
		socket = new ServerSocket(port);
		accepter.start();
	}

	protected void handleConnection(Socket connection) throws IOException {
		SoundServerClient client = new SoundServerClient(connection, this);
		connections.add(client);
	}

	void sendSound(byte[] data, SoundServerClient sender) {
		for (SoundServerClient ssc : connections)
			if (ssc != sender)
				try {
					ssc.sendData(data);
				} catch (IOException e) {
					e.printStackTrace();
				}
	}

}
