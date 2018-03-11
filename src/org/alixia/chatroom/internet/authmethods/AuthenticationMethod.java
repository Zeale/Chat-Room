package org.alixia.chatroom.internet.authmethods;

import java.io.IOException;
import java.util.UUID;

import org.alixia.chatroom.internet.AuthServer;

/**
 * A class that contains ways for client classes to contact an
 * {@link AuthServer} and, login to an account, or verify an account's login.
 *
 * @author Zeale
 *
 */
public abstract class AuthenticationMethod {
	public static class AuthenticationResult {

		public final boolean verified;

		public AuthenticationResult(final boolean verified) {
			this.verified = verified;
		}

	}

	public static class LoginResult {
		public enum ErrorType {
			USERNAME_NOT_FOUND, WRONG_PASSWORD, TIMEOUT;
		}

		public final UUID sessionID;

		public final ErrorType errType;

		public LoginResult(final UUID sessionID, final ErrorType errType) {
			this.sessionID = sessionID;
			this.errType = errType;
		}

		public boolean isSuccessful() {
			return sessionID != null;
		}
	}

	/**
	 * Verifies a user's login. This should be called by a ChatRoom host when a user
	 * tries to connect. A sessionID is given to the ChatRoom host which will verify
	 * the sessionID with an authentication server.
	 *
	 * @param username
	 *            The user's username.
	 * @param sessionID
	 *            The user's sessionID.
	 * @return An {@link AuthenticationResult} containing data retrieved from the
	 *         authentication attempt.
	 * @throws IOException
	 *             If any error occurs.
	 */
	public abstract AuthenticationResult authenticate(String username, UUID sessionID) throws IOException;

	/**
	 * Tries to log a user into their account. This should be called by a client
	 * instance of this program which is trying to log its user in to an
	 * authentication server.
	 *
	 * @param username
	 *            The user's username.
	 * @param password
	 *            The user's password.
	 * @return A {@link LoginResult} containing data retrieved from the login
	 *         attempt.
	 * @throws IOException
	 *             If any error occurs.
	 */
	public abstract LoginResult login(String username, String password) throws IOException;

}
