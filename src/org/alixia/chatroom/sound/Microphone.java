package org.alixia.chatroom.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Microphone {

	private final TargetDataLine microphone;
	private boolean recording;

	public boolean isRecording() {
		return recording;
	}

	public static void main(String[] args) throws LineUnavailableException {

		Microphone mic = getCurrentMicrophone();
		System.out.println(mic.microphone);

	}

	public Microphone(TargetDataLine microphone) {
		this.microphone = microphone;
	}

	public Sound record() {
		Sound sound = new Sound();

		return sound;
	}

	public Sound record(long nanosecs) {
		if (isRecording())
			throw new IllegalStateException("This microphone is already recording something...");
		Sound sound = new Sound();

		Thread recorder = new Thread(new Runnable() {

			@Override
			public void run() {

				while (true) {
					if (Thread.interrupted())
						break;
					// Record into our sound object.

				}
			}
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

	private void startRecording() {
		recording = true;
	}

	private void stopRecording() {
		recording = false;
	}

	public static Microphone getCurrentMicrophone() throws LineUnavailableException {
		AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
		TargetDataLine microphone = AudioSystem.getTargetDataLine(format);

		return new Microphone(microphone);
	}
}
