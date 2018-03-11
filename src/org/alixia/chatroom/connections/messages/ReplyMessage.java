package org.alixia.chatroom.connections.messages;

public class ReplyMessage extends Message {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public ReplyMessage(final Message message) {
		super(message.id);
	}

}
