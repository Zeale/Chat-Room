package org.alixia.chatroom.internet;

public class LoginRequestPacket {
	
	public final String username, password;

	public LoginRequestPacket(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
}
