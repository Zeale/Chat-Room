package org.alixia.chatroom.internet.authmethods;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.UUID;

import org.alixia.chatroom.internet.LoginRequestPacket;
import org.alixia.chatroom.internet.SessionIDPacket;
import org.alixia.chatroom.internet.SessionIDPacket.Success;
import org.alixia.chatroom.internet.VerificationPacket;
import org.alixia.chatroom.internet.VerificationRequestPacket;
import org.alixia.chatroom.internet.authmethods.AuthenticationMethod.LoginResult.ErrorType;

public class AppAuthMethodImpl extends AuthenticationMethod {

	private final String host;
	private final int port;

	public AppAuthMethodImpl(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public LoginResult login(String username, String password) throws IOException {

		// Create our connection objects
		Socket socket = new Socket(host, port);
		ObjectOutputStream sender = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());

		// Send the login request
		sender.writeObject(new LoginRequestPacket(username, password));
		sender.flush();

		try {

			// Get the reply.
			SessionIDPacket reply = (SessionIDPacket) reader.readObject();

			return reply.success == Success.SUCCESS ? new LoginResult(reply.sessionID, null)
					: new LoginResult(null,
							reply.success == Success.USERNAME_NOT_FOUND ? LoginResult.ErrorType.USERNAME_NOT_FOUND
									: LoginResult.ErrorType.WRONG_USERNAME);

		} catch (SocketTimeoutException e) {
			// Catch timeouts
			return new LoginResult(null, ErrorType.TIMEOUT);
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			socket.close();
		}
	}

	@Override
	public AuthenticationResult authenticate(String username, UUID sessionID) throws IOException {

		Socket sock = new Socket(host, port);
		ObjectOutputStream sender = new ObjectOutputStream(sock.getOutputStream());
		ObjectInputStream reader = new ObjectInputStream(sock.getInputStream());

		sender.writeObject(new VerificationRequestPacket(username, sessionID));
		sender.flush();

		try {
			VerificationPacket reply = (VerificationPacket) reader.readObject();

			return new AuthenticationResult(reply.verified);
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			sock.close();
		}

	}

}
