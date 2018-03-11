package org.alixia.chatroom.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Microphone {

	public static Microphone getCurrentMicrophone() throws LineUnavailableException {
		final AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
		final TargetDataLine microphone = AudioSystem.getTargetDataLine(format);

		return new Microphone(microphone);
	}

	public static void main(final String[] args) throws LineUnavailableException {

		final Microphone mic = getCurrentMicrophone();
		System.out.println(mic.microphone);

	}

	private final TargetDataLine microphone;

	private boolean recording;

	public Microphone(final TargetDataLine microphone) {
		this.microphone = microphone;
	}

	public boolean isRecording() {
		return recording;
	}

	public Sound record() {
		final Sound sound = new Sound();

		return sound;
	}

	public Sound record(final long nanosecs) {
		if (isRecording())
			throw new IllegalStateException("This microphone is already recording something...");
		final Sound sound = new Sound();

		final Thread recorder = new Thread(() -> {

			while (true)
				if (Thread.interrupted())
					break;
			// Record into our sound object.
		});
		new Thread(new Runnable() {

			private long start;

			@Override
			public void run() {
				start = System.nanoTime();
				recorder.start();
				while (System.nanoTime() - start < nanosecs)
					;
				recorder.interrupt();
			}
		}).start();

		return sound;

	}
}
