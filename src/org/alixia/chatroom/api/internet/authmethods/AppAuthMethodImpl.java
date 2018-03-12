package org.alixia.chatroom.api.internet.authmethods;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.UUID;

import org.alixia.chatroom.api.internet.CreateAccountReplyPacket;
import org.alixia.chatroom.api.internet.CreateAccountRequestPacket;
import org.alixia.chatroom.api.internet.LoginRequestPacket;
import org.alixia.chatroom.api.internet.LogoutReplyPacket;
import org.alixia.chatroom.api.internet.LogoutRequestPacket;
import org.alixia.chatroom.api.internet.SessionIDPacket;
import org.alixia.chatroom.api.internet.VerificationPacket;
import org.alixia.chatroom.api.internet.VerificationRequestPacket;
import org.alixia.chatroom.api.internet.CreateAccountReplyPacket.ErrType;
import org.alixia.chatroom.api.internet.LogoutReplyPacket.ErrorType;
import org.alixia.chatroom.api.internet.SessionIDPacket.Success;
import org.alixia.chatroom.api.internet.authmethods.exceptions.AccountCreationDeniedException;
import org.alixia.chatroom.api.internet.authmethods.exceptions.IncorrectPasswordException;
import org.alixia.chatroom.api.internet.authmethods.exceptions.InvalidSessionIDException;
import org.alixia.chatroom.api.internet.authmethods.exceptions.InvalidUsernameException;
import org.alixia.chatroom.api.internet.authmethods.exceptions.TimeoutException;
import org.alixia.chatroom.api.internet.authmethods.exceptions.UnknownAuthenticationException;
import org.alixia.chatroom.api.internet.authmethods.exceptions.UsernameNotFoundException;
import org.alixia.chatroom.api.internet.authmethods.exceptions.UsernameTakenException;

public class AppAuthMethodImpl extends AuthenticationMethod {

	private final String host;
	private final int port;

	public AppAuthMethodImpl(final String host, final int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public boolean authenticate(final String username, final UUID sessionID) throws IOException, ConnectException {

		final Socket sock = new Socket(host, port);
		sock.setSoTimeout(getTimeout());
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
	public UUID login(final String username, final String password) throws IOException, TimeoutException,
			UsernameNotFoundException, IncorrectPasswordException, ConnectException {

		// Create our connection objects
		final Socket socket = new Socket(host, port);
		socket.setSoTimeout(getTimeout());
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
	public UUID createNewAccount(String username, String password) throws IOException, TimeoutException,
			UsernameTakenException, ConnectException, InvalidUsernameException, AccountCreationDeniedException {
		// Create our connection objects
		final Socket socket = new Socket(host, port);
		socket.setSoTimeout(getTimeout());
		final ObjectOutputStream sender = new ObjectOutputStream(socket.getOutputStream());
		final ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());

		sender.writeObject(new CreateAccountRequestPacket(username, password));
		sender.flush();

		final CreateAccountReplyPacket reply;

		try {
			reply = (CreateAccountReplyPacket) reader.readObject();
		} catch (SocketTimeoutException e) {
			throw new TimeoutException();
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			socket.close();
		}

		if (reply.error == null)
			return reply.sessionID;
		else if (reply.error == ErrType.INVALID_USERNAME)
			throw new InvalidUsernameException();
		else if (reply.error == ErrType.REQUEST_DENIED)
			throw new AccountCreationDeniedException();
		else if (reply.error == ErrType.USERNAME_EXISTS)
			throw new UsernameTakenException();
		else
			throw new UnknownAuthenticationException();
	}

	@Override
	public void logout(String username, UUID sessionID) throws IOException, UsernameNotFoundException, TimeoutException,
			UnknownAuthenticationException, InvalidSessionIDException {
		// Create our connection objects
		final Socket socket = new Socket(host, port);
		socket.setSoTimeout(getTimeout());
		final ObjectOutputStream sender = new ObjectOutputStream(socket.getOutputStream());
		final ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());

		sender.writeObject(new LogoutRequestPacket(username, sessionID));
		sender.flush();
		final LogoutReplyPacket reply;
		try {
			reply = (LogoutReplyPacket) reader.readObject();
		} catch (SocketTimeoutException e) {
			throw new TimeoutException();
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			socket.close();
		}
		if (!reply.isSuccessful())
			if (reply.error == ErrorType.INVALID_SESSION_ID)
				throw new InvalidSessionIDException("SessionID: " + sessionID);
			else if (reply.error == ErrorType.USERNAME_NOT_FOUND)
				throw new UsernameNotFoundException("Username: " + username);
	}

}
