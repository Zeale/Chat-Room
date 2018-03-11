package org.alixia.chatroom.internet.authmethods;

import java.io.IOException;
import java.util.UUID;

import org.alixia.chatroom.internet.Authentication;
import org.alixia.chatroom.internet.BasicAuthServer;
import org.alixia.chatroom.internet.authmethods.exceptions.AccountCreationDeniedException;
import org.alixia.chatroom.internet.authmethods.exceptions.IncorrectPasswordException;
import org.alixia.chatroom.internet.authmethods.exceptions.InvalidSessionIDException;
import org.alixia.chatroom.internet.authmethods.exceptions.InvalidUsernameException;
import org.alixia.chatroom.internet.authmethods.exceptions.TimeoutException;
import org.alixia.chatroom.internet.authmethods.exceptions.UnknownAuthenticationException;
import org.alixia.chatroom.internet.authmethods.exceptions.UsernameNotFoundException;
import org.alixia.chatroom.internet.authmethods.exceptions.UsernameTakenException;

/**
 * A class that contains ways for client classes to contact an
 * {@link BasicAuthServer} and, login to an account, or verify an account's
 * login.
 *
 * @author Zeale
 *
 */
public abstract class AuthenticationMethod {

	private int timeout = Authentication.DEFAULT_TIMEOUT_MILLIS;

	public int getTimeout() {
		return timeout;
	}

	protected void setTimeout(int timeout) {
		this.timeout = timeout;
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
	 *             If any non-authentication-related error occurs.
	 */
	public abstract boolean authenticate(String username, UUID sessionID)
			throws IOException, TimeoutException, UsernameNotFoundException, UnknownAuthenticationException;

	/**
	 * Tries to log a user into their account. This should be called by a client
	 * instance of this program which is trying to log its user in to an
	 * authentication server.
	 *
	 * @param username
	 *            The user's username.
	 * @param password
	 *            The user's password.
	 * @return The sessionID of the account if successful and <code>null</code>
	 *         instead, if the login is unsuccessful.
	 * @throws IOException
	 *             If any non-authentication-related error occurs.
	 */
	public abstract UUID login(String username, String password) throws IOException, IncorrectPasswordException,
			UsernameNotFoundException, TimeoutException, UnknownAuthenticationException;

	public abstract UUID createNewAccount(String username, String password)
			throws IOException, TimeoutException, UsernameTakenException, InvalidUsernameException,
			UnknownAuthenticationException, AccountCreationDeniedException;

	public abstract void logout(String username, UUID sessionID) throws IOException, UsernameNotFoundException,
			TimeoutException, UnknownAuthenticationException, InvalidSessionIDException;

}
