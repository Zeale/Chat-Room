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

	public AppAuthMethodImpl(final String host, final int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public AuthenticationResult authenticate(final String username, final UUID sessionID) throws IOException {

		final Socket sock = new Socket(host, port);
		final ObjectOutputStream sender = new ObjectOutputStream(sock.getOutputStream());
		final ObjectInputStream reader = new ObjectInputStream(sock.getInputStream());

		sender.writeObject(new VerificationRequestPacket(username, sessionID));
		sender.flush();

		try {
			final VerificationPacket reply = (VerificationPacket) reader.readObject();

			return new AuthenticationResult(reply.verified);
		} catch (final Exception e) {
			throw new IOException(e);
		} finally {
			sock.close();
		}

	}

	@Override
	public LoginResult login(final String username, final String password) throws IOException {

		// Create our connection objects
		final Socket socket = new Socket(host, port);
		final ObjectOutputStream sender = new ObjectOutputStream(socket.getOutputStream());
		final ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());

		// Send the login request
		sender.writeObject(new LoginRequestPacket(username, password));
		sender.flush();

		try {

			// Get the reply.
			final SessionIDPacket reply = (SessionIDPacket) reader.readObject();

			return reply.success == Success.SUCCESS ? new LoginResult(reply.sessionID, null)
					: new LoginResult(null,
							reply.success == Success.USERNAME_NOT_FOUND ? LoginResult.ErrorType.USERNAME_NOT_FOUND
									: LoginResult.ErrorType.WRONG_PASSWORD);

		} catch (final SocketTimeoutException e) {
			// Catch timeouts
			return new LoginResult(null, ErrorType.TIMEOUT);
		} catch (final Exception e) {
			throw new IOException(e);
		} finally {
			socket.close();
		}
	}

}
