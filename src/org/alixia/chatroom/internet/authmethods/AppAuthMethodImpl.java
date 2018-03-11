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
import org.alixia.chatroom.internet.authmethods.exceptions.IncorrectPasswordException;
import org.alixia.chatroom.internet.authmethods.exceptions.TimeoutException;
import org.alixia.chatroom.internet.authmethods.exceptions.UnknownAuthenticationException;
import org.alixia.chatroom.internet.authmethods.exceptions.UsernameNotFoundException;

public class AppAuthMethodImpl extends AuthenticationMethod {

	private final String host;
	private final int port;

	public AppAuthMethodImpl(final String host, final int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public boolean authenticate(final String username, final UUID sessionID) throws IOException {

		final Socket sock = new Socket(host, port);
		final ObjectOutputStream sender = new ObjectOutputStream(sock.getOutputStream());
		final ObjectInputStream reader = new ObjectInputStream(sock.getInputStream());

		sender.writeObject(new VerificationRequestPacket(username, sessionID));
		sender.flush();

		try {
			final VerificationPacket reply = (VerificationPacket) reader.readObject();

			return reply.verified;
		} catch (final Exception e) {
			throw new IOException(e);
		} finally {
			sock.close();
		}

	}

	@Override
	public UUID login(final String username, final String password)
			throws IOException, TimeoutException, UsernameNotFoundException, IncorrectPasswordException {

		// Create our connection objects
		final Socket socket = new Socket(host, port);
		final ObjectOutputStream sender = new ObjectOutputStream(socket.getOutputStream());
		final ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());

		// Send the login request
		sender.writeObject(new LoginRequestPacket(username, password));
		sender.flush();
		final SessionIDPacket reply;
		try {
			// Get the reply.
			reply = (SessionIDPacket) reader.readObject();
		} catch (final SocketTimeoutException e) {
			throw new TimeoutException();
		} catch (final Exception e) {
			throw new IOException(e);
		} finally {
			socket.close();
		}

		if (reply.success == Success.SUCCESS)
			return reply.sessionID;
		else if (reply.success == Success.USERNAME_NOT_FOUND)
			throw new UsernameNotFoundException("Username: " + username);
		else if (reply.success == Success.WRONG_PASSWORD)
			throw new IncorrectPasswordException();
		else
			throw new UnknownAuthenticationException();
	}

	@Override
	public UUID createNewAccount(String username, String password)
			throws IOException, TimeoutException, UsernameTakenException {
		// TODO Auto-generated method stub
		return null;
	}

}
