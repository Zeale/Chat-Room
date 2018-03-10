package org.alixia.chatroom.internet;

import java.io.IOException;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.Account;
import org.alixia.chatroom.internet.authmethods.AppAuthMethodImpl;
import org.alixia.chatroom.internet.authmethods.AuthenticationMethod;
import org.alixia.chatroom.internet.authmethods.AuthenticationMethod.LoginResult;
import org.alixia.chatroom.internet.authmethods.AuthenticationMethod.LoginResult.ErrorType;
import org.alixia.chatroom.logging.Logger;

public final class Authentication {

	public static final int DEFAULT_AUTHENTICATION_PORT = 35560;
	public static final String DEFAULT_AUTHENTICATION_SERVER = "dusttoash.org";

	private static final AuthenticationMethod DEFAULT_AUTHENTICATION_METHOD = new AppAuthMethodImpl(
			DEFAULT_AUTHENTICATION_SERVER, DEFAULT_AUTHENTICATION_PORT);

	public static Logger LOGGER = new Logger("AUTHENTICATER");

	private static AuthenticationMethod authMethod = DEFAULT_AUTHENTICATION_METHOD;

	private static AuthServer server;

	private Authentication() {
	}

	public static AuthenticationMethod getDefaultAuthenticationMethod() {
		return authMethod;
	}

	/**
	 * Attempts to log the user in given the specified parameters. Results are
	 * printed to the console using {@link #LOGGER}.
	 * 
	 * @param username
	 *            The user's username.
	 * @param password
	 *            The user's password.
	 */
	public static void login(String username, String password) {
		LoginResult result;
		try {
			result = Authentication.getDefaultAuthenticationMethod().login(username, password);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.log("An error occurred...");
			return;
		}

		if (result.isSuccessful()) {
			LOGGER.log("Successfully logged in!");
			if (ChatRoom.INSTANCE.clients.isItemSelected()) {
				ChatRoom.INSTANCE.setAccount(new Account(username, result.sessionID));
				ChatRoom.INSTANCE.clients.getSelectedItem().sendObject(ChatRoom.INSTANCE.getAccount());
			}
		} else if (result.errType == ErrorType.TIMEOUT)
			LOGGER.log("Could not connect to server...");
		else if (result.errType == ErrorType.USERNAME_NOT_FOUND)
			LOGGER.log("Username not found");
		else if (result.errType == ErrorType.WRONG_PASSWORD)
			LOGGER.log("Wrong password");
	}

	public static void setDefaultAuthenticationMethod(AuthenticationMethod method) {
		if (method == null)
			throw new IllegalArgumentException();
		authMethod = method;
	}

	public static void startAuthServer(int port) throws IOException {
		if (server == null)
			server = new AuthServer(port);
	}

	public static AuthServer getAuthServer() {
		return server;
	}
}
