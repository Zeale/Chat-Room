package org.alixia.chatroom.internet;

import java.io.IOException;
import java.net.ConnectException;
import java.util.UUID;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.Account;
import org.alixia.chatroom.internet.authmethods.AppAuthMethodImpl;
import org.alixia.chatroom.internet.authmethods.AuthenticationMethod;
import org.alixia.chatroom.internet.authmethods.exceptions.IncorrectPasswordException;
import org.alixia.chatroom.internet.authmethods.exceptions.TimeoutException;
import org.alixia.chatroom.internet.authmethods.exceptions.UsernameNotFoundException;
import org.alixia.chatroom.logging.Logger;

public final class Authentication {

	public static final int DEFAULT_TIMEOUT_MILLIS = 5000;

	public static final int DEFAULT_AUTHENTICATION_PORT = 35560;
	public static final String DEFAULT_AUTHENTICATION_SERVER = "dusttoash.org";

	private static final AuthenticationMethod DEFAULT_AUTHENTICATION_METHOD = new AppAuthMethodImpl(
			DEFAULT_AUTHENTICATION_SERVER, DEFAULT_AUTHENTICATION_PORT);

	public static Logger LOGGER = new Logger("AUTHENTICATER");

	private static AuthenticationMethod authMethod = DEFAULT_AUTHENTICATION_METHOD;

	private static BasicAuthServer server;

	public static BasicAuthServer getAuthServer() {
		return server;
	}

	public static boolean isAuthServerRunning() {
		return server != null;
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
	public static void login(final String username, final String password) {
		try {
			UUID result = Authentication.getDefaultAuthenticationMethod().login(username, password);
			LOGGER.log("Successfully logged in!");
			if (ChatRoom.INSTANCE.clients.isItemSelected()) {
				ChatRoom.INSTANCE.setAccount(new Account(username, result));
				ChatRoom.INSTANCE.clients.getSelectedItem().sendObject(ChatRoom.INSTANCE.getAccount());
			}
		} catch (ConnectException e) {
			LOGGER.log("Failed to connect to the authentication server.");
		} catch (final IOException e) {
			e.printStackTrace();
			LOGGER.log("An unknown error occurred...");
			return;
		} catch (TimeoutException e) {
			LOGGER.log("Could not connect to server...");
		} catch (UsernameNotFoundException e) {
			LOGGER.log("Username not found");
		} catch (IncorrectPasswordException e) {
			LOGGER.log("Wrong password");
		}

	}

	public static void setDefaultAuthenticationMethod(final AuthenticationMethod method) {
		if (method == null)
			throw new IllegalArgumentException();
		authMethod = method;
	}

	public static void startAuthServer(final int port) throws IOException {
		if (server == null)
			server = new BasicAuthServer(port);
	}

	public static void closeAuthServer() {
		if (!isAuthServerRunning())
			return;
		server.close();
		server = null;
	}

	public static void kickstartAuthServer() {
		if (server != null && !server.isRunning())
			server.start();
	}

	private Authentication() {
	}
}
