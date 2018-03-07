package org.alixia.chatroom.impl.guis.settings;

import java.io.IOException;

import org.alixia.chatroom.internet.Authentication;

public class Settings extends SettingsWindowImpl {

	@Override
	public boolean handleLogin(String username, String password) throws IOException {
		return Authentication.getDefaultAuthenticationMethod().login(username, password).isSuccessful();
	}

}
