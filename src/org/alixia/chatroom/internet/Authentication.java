package org.alixia.chatroom.internet;

import org.alixia.chatroom.internet.authmethods.AuthenticationMethod;

public final class Authentication {

	public static final int DEFAULT_AUTHENTICATION_PORT = 35560;
	public static final String DEFAULT_AUTHENTICATION_SERVER = "dusttoash.org";

	private Authentication() {
	}

	public static AuthenticationMethod getDefaultAuthenticationMethod() {
		// TODO Get default method
		return null;
	}
}
