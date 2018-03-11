package org.alixia.chatroom.internet.authmethods;

import java.util.UUID;

public class LoginResult {
	public enum ErrorType {
		USERNAME_NOT_FOUND, WRONG_PASSWORD, TIMEOUT;
	}

	public final UUID sessionID;

	public final LoginResult.ErrorType errType;

	public LoginResult(final UUID sessionID, final LoginResult.ErrorType errType) {
		this.sessionID = sessionID;
		this.errType = errType;
	}

	public boolean isSuccessful() {
		return sessionID != null;
	}
}