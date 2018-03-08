package org.alixia.chatroom.internet;

import java.io.IOException;

import org.alixia.chatroom.internet.authmethods.AppAuthMethodImpl;
import org.alixia.chatroom.internet.authmethods.AuthenticationMethod;

public final class Authentication {

	public static final int DEFAULT_AUTHENTICATION_PORT = 35560;
	public static final String DEFAULT_AUTHENTICATION_SERVER = "dusttoash.org";

	private static final AuthenticationMethod DEFAULT_AUTHENTICATION_METHOD = new AppAuthMethodImpl(
			DEFAULT_AUTHENTICATION_SERVER, DEFAULT_AUTHENTICATION_PORT);

	private static AuthenticationMethod authMethod = DEFAULT_AUTHENTICATION_METHOD;

	private static AuthServer server;

	private Authentication() {
	}

	public static AuthenticationMethod getDefaultAuthenticationMethod() {
		return authMethod;
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
}
