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

import javafx.application.Platform;

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
		// We don't want to vex the application thread. :P
		if (Platform.isFxApplicationThread()) {
			new Thread(() -> login(username, password)).start();
			return;
		}
		try {
			UUID result = Authentication.getDefaultAuthenticationMethod().login(username, password);
			LOGGER.log("Successfully logged in!");
			if (ChatRoom.INSTANCE.clients.isItemSelected()) {
				ChatRoom.INSTANCE.setAccount(new Account(username, result));
				ChatRoom.INSTANCE.clients.getSelectedItem().sendObject(ChatRoom.INSTANCE.getAccount());
			}
		} catch (ConnectException e) {
			LOGGER.log(
					"Failed to connect to the authentication server. (The server might be down. If it isn't, you might not be connected to the internet. If you are, then for some weird reason, the server couldn't be connected to.)");
		} catch (final IOException e) {
			e.printStackTrace();
			LOGGER.log(
					"An unknown error occurred... The stack trace has been printed to the console. If you have access to the stacktrace, you should send it to the developer.");
			return;
		} catch (TimeoutException e) {
			LOGGER.log("The server was successfully connected to, but the connection timed out. The timeout is set to "
					+ (double) Authentication.getDefaultAuthenticationMethod().getTimeout() / 1000 + " seconds.");
		} catch (UsernameNotFoundException e) {
			LOGGER.log("That username is not registered.");
		} catch (IncorrectPasswordException e) {
			LOGGER.log("Incorrect password.");
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
