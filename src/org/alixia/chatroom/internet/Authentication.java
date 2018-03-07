package org.alixia.chatroom.internet;

import org.alixia.chatroom.internet.authmethods.AppAuthMethodImpl;
import org.alixia.chatroom.internet.authmethods.AuthenticationMethod;

public final class Authentication {

	public static final int DEFAULT_AUTHENTICATION_PORT = 35560;
	public static final String DEFAULT_AUTHENTICATION_SERVER = "dusttoash.org";

	private static final AuthenticationMethod DEFAULT_AUTHENTICATION_METHOD = new AppAuthMethodImpl(
			DEFAULT_AUTHENTICATION_SERVER, DEFAULT_AUTHENTICATION_PORT);

	private Authentication() {
	}

	public static AuthenticationMethod getDefaultAuthenticationMethod() {
		return DEFAULT_AUTHENTICATION_METHOD;
	}
}
