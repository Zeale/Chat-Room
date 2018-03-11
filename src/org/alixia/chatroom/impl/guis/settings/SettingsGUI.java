package org.alixia.chatroom.impl.guis.settings;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.internet.Authentication;
import org.alixia.chatroom.logging.Logger;

import javafx.scene.paint.Color;

public class SettingsGUI extends _SettingsWindowImpl {

	public static final Logger LOGGER = new Logger("SETTINGS", ChatRoom.LOGGER);
	static {
		LOGGER.boldHeader = true;
		LOGGER.bracketColor = LOGGER.separatorColor = Color.GOLD;
		LOGGER.parentColor = Color.RED;
		LOGGER.childColor = Color.BLUE;
		LOGGER.messageColor = Color.GREEN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alixia.chatroom.impl.guis.settings._SettingsWindowImpl#handleLogin(java.
	 * lang.String, java.lang.String)
	 */
	@Override
	public void handleLogin(String username, String password) {
		Authentication.login(username, password);
	}

}
