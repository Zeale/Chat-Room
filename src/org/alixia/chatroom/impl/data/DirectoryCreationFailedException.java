package org.alixia.chatroom.impl.data;

import java.io.File;

public class DirectoryCreationFailedException extends Exception {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public final File directory;

	public DirectoryCreationFailedException(final File directory) {
		this.directory = directory;
	}

	public DirectoryCreationFailedException(final String message, final File directory) {
		super(message);
		this.directory = directory;
	}

	public DirectoryCreationFailedException(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace, final File directory) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.directory = directory;
	}

	public DirectoryCreationFailedException(final String message, final Throwable cause, final File directory) {
		super(message, cause);
		this.directory = directory;
	}

	public DirectoryCreationFailedException(final Throwable cause, final File directory) {
		super(cause);
		this.directory = directory;
	}

}
