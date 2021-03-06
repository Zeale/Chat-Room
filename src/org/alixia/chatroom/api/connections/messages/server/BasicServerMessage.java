package org.alixia.chatroom.api.connections.messages.server;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.Console;
import org.alixia.chatroom.api.connections.Server;
import org.alixia.chatroom.api.logging.Logger;
import org.alixia.chatroom.api.texts.ConsoleText;

public class BasicServerMessage extends ServerMessage {

	public static final Logger SERVER_LOGGER = Server.SERVER_LOGGER;

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;
	public String message;

	public BasicServerMessage(final String message) {
		this.message = message;
	}

	/**
	 *
	 * Note that messages are only printed to {@link ChatRoom#console}'s console,
	 * regardless of this method's parameters. This is due to the nature of the
	 * {@link Logger} class, which is implemented by this method.
	 *
	 */
	@Override
	public ConsoleText toConsoleText() {
		return new ConsoleText() {

			@Override
			public void print(final Console console) {
				SERVER_LOGGER.log(message);
			}

		};
	}

}
