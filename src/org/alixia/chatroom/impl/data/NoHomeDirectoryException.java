package org.alixia.chatroom.impl.data;

public class NoHomeDirectoryException extends RuntimeException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;
	
	public NoHomeDirectoryException() {
		super("No home directory has been set yet.");
	}

}
