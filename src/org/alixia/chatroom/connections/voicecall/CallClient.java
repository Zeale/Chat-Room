package org.alixia.chatroom.connections.voicecall;

import java.io.IOException;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class CallClient {

	private final int CHUNK_SIZE = 4096;

	private final Socket socket;

	private final AudioInputStream ais;
	private final SourceDataLine speaker;

	private final TargetDataLine mic;

	private boolean mute, deaf;

	public CallClient(String hostname, int port, AudioFormat format) throws LineUnavailableException, IOException {
		this.socket = new Socket(hostname, port);

		ais = new AudioInputStream(socket.getInputStream(), format, Integer.MAX_VALUE);
		speaker = AudioSystem.getSourceDataLine(format);
		speaker.open(format);
		speaker.start();

		mic = AudioSystem.getTargetDataLine(format);
		mic.open(format);
		mic.start();

		recorder.start();
		receiver.start();

	}

	private Thread recorder = new Thread(new Runnable() {

		@Override
		public void run() {
			while (!mute) {
				byte[] data = new byte[CHUNK_SIZE];
				int size = mic.read(data, 0, data.length);
				try {
					System.out.println("CALLCLIENT: Wrote " + size + " data to the server.");
					socket.getOutputStream().write(data, 0, size);
					socket.getOutputStream().flush();
				} catch (IOException e) {
					System.err.println("FAILED TO SEND SOME SOUND TO THE SERVER.");
					e.printStackTrace();
				}
			}
			recorder = new Thread(this);
			recorder.setDaemon(true);
		}
	});

	private Thread receiver = new Thread(new Runnable() {

		@Override
		public void run() {
			byte[] buffer = new byte[65000];
			int cnt;
			try {
				while (true) {
					// If we're deaf, trash the sound; we don't play it to the speaker.
					cnt = ais.read(deaf ? new byte[buffer.length] : buffer, 0, buffer.length);
					if (cnt == -1) {
						Thread.sleep(10);
						continue;
					}
					if (!deaf)
						speaker.write(buffer, 0, cnt);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			receiver = new Thread(this);
			receiver.setDaemon(true);
		}
	});
	{
		receiver.setDaemon(true);
		recorder.setDaemon(true);
	}

	public void deafen() {
		deaf = true;
	}

	public void undeafen() {
		deaf = false;
		if (!receiver.isAlive())
			receiver.start();
	}

	public void unmute() {
		mute = false;
		if (!recorder.isAlive())
			recorder.start();
	}

	public void mute() {
		mute = true;
	}

}
