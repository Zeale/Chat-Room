package org.alixia.chatroom.connections.voicecall;

import java.io.IOException;
import java.net.Socket;

class SoundServerClient {

	private final Socket socket;

	private final CallServer owner;

	public SoundServerClient(Socket socket, CallServer owner) throws IOException {

		this.socket = socket;
		this.owner = owner;
		receiver.start();

	}

	private Thread receiver = new Thread(new Runnable() {

		@Override
		public void run() {
			while (true) {
				try {
					byte[] data = new byte[socket.getInputStream().available()];
					socket.getInputStream().read(data);
					owner.sendSound(data, SoundServerClient.this);
				} catch (IOException e) {
					e.printStackTrace();
					try {
						Thread.sleep(10);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	});

	{
		receiver.setDaemon(true);
	}

	public void sendData(byte[] data) throws IOException {
		// With a sysout, we can see that data.length is 0. Sound is still sent,
		// however...
		socket.getOutputStream().write(data);
		socket.getOutputStream().flush();
	}

}
