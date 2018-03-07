package org.alixia.chatroom.internet.authmethods;

import java.util.UUID;

public abstract class AuthenticationMethod {
	public enum LoginResult {
		SUCCESS, WRONG_PASSWORD, WRONG_USERNAME;
	}

	public enum AuthenticationResult {
		VALID_ID, INVALID_ID, USERNAME_NOT_FOUND;
	}

	public abstract LoginResult login(String username, String password);

	public abstract AuthenticationResult authenticate(String username, UUID sessionID);

}
