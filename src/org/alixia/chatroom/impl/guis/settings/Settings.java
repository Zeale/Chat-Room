package org.alixia.chatroom.impl.guis.settings;

import org.alixia.chatroom.internet.Authentication;
import org.alixia.chatroom.internet.authmethods.AuthenticationMethod.LoginResult;

public class Settings extends SettingsWindowImpl {

	@Override
	public boolean handleLogin(String username, String password) {
		return Authentication.getDefaultAuthenticationMethod().login(username, password) == LoginResult.SUCCESS;
	}

}
