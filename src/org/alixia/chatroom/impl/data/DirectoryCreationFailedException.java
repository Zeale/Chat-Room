package org.alixia.chatroom.impl.data;

import java.io.File;

public class DirectoryCreationFailedException extends Exception {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public final File directory;

	public DirectoryCreationFailedException(File directory) {
		this.directory = directory;
	}

	public DirectoryCreationFailedException(String message, File directory) {
		super(message);
		this.directory = directory;
	}

	public DirectoryCreationFailedException(Throwable cause, File directory) {
		super(cause);
		this.directory = directory;
	}

	public DirectoryCreationFailedException(String message, Throwable cause, File directory) {
		super(message, cause);
		this.directory = directory;
	}

	public DirectoryCreationFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace, File directory) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.directory = directory;
	}
	
	

}
