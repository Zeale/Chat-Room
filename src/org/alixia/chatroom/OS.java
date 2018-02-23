package org.alixia.chatroom;

public enum OS {

	WINDOWS, MAC;

	// TODO This will need to be updated.
	public static OS getOS() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("win"))
			return WINDOWS;
		else
			return MAC;
	}

}
