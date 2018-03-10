package org.alixia.chatroom.impl.guis.settings;

import java.io.IOException;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.Account;
import org.alixia.chatroom.fxtools.FXTools;
import org.alixia.chatroom.internet.Authentication;
import org.alixia.chatroom.internet.authmethods.AuthenticationMethod.LoginResult;
import org.alixia.chatroom.internet.authmethods.AuthenticationMethod.LoginResult.ErrorType;

public class Settings extends _SettingsWindowImpl {

	@Override
	public void handleLogin(String username, String password) {
		LoginResult result;
		try {
			result = Authentication.getDefaultAuthenticationMethod().login(username, password);
		} catch (IOException e) {
			e.printStackTrace();
			FXTools.spawnLabelAtMousePos("An error occurred...", ChatRoom.ERROR_COLOR, this);
			return;
		}

		if (result.isSuccessful())
			FXTools.spawnLabelAtMousePos("Successfully logged in!", ChatRoom.SUCCESS_COLOR, this);
		else if (result.errType == ErrorType.TIMEOUT)
			FXTools.spawnLabelAtMousePos("Could not connect to server...", ChatRoom.ERROR_COLOR, this);
		else if (result.errType == ErrorType.USERNAME_NOT_FOUND)
			FXTools.spawnLabelAtMousePos("Username not found", ChatRoom.ERROR_COLOR, this);
		else if (result.errType == ErrorType.WRONG_PASSWORD)
			FXTools.spawnLabelAtMousePos("Wrong password", ChatRoom.ERROR_COLOR, this);

		ChatRoom.INSTANCE.setAccount(new Account(username, result.sessionID));

	}

}
