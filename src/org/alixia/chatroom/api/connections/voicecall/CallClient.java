package org.alixia.chatroom.api.connections.voicecall;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

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

	private boolean run = true;

	private Thread recorder = new Thread(new Runnable() {

		@Override
		public void run() {
			while (!mute && run) {
				final byte[] data = new byte[CHUNK_SIZE];
				final int size = mic.read(data, 0, data.length);
				try {
					socket.getOutputStream().write(data, 0, size);
					socket.getOutputStream().flush();
				} catch (final SocketException e) {
					run = false;
				} catch (final IOException e) {
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
			final byte[] buffer = new byte[65000];
			int cnt;
			try {
				while (run) {
					// If we're deaf, trash the sound; we don't play it to the speaker.
					cnt = ais.read(deaf ? new byte[buffer.length] : buffer, 0, buffer.length);
					if (cnt == -1) {
						Thread.sleep(10);
						continue;
					}
					if (!deaf)
						speaker.write(buffer, 0, cnt);
				}
			} catch (final Exception e) {
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

	public CallClient(final String hostname, final int port, final AudioFormat format)
			throws LineUnavailableException, IOException {
		socket = new Socket(hostname, port);

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

	public void deafen() {
		deaf = true;
	}

	public void disconnect() throws IOException {
		run = false;
		ais.close();
		socket.close();
		speaker.close();
		mic.close();
	}

	public void mute() {
		mute = true;
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

}
