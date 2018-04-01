package org.alixia.chatroom.impl.data;

public class LocalInstallDirectoryBuggedException extends RuntimeException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public final String buggedDirectory;

	public LocalInstallDirectoryBuggedException(final String buggedDirectory) {
		this.buggedDirectory = buggedDirectory;
	}

	public LocalInstallDirectoryBuggedException(final String message, final String buggedDirectory) {
		super(message);
		this.buggedDirectory = buggedDirectory;
	}

	public LocalInstallDirectoryBuggedException(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace, final String buggedDirectory) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.buggedDirectory = buggedDirectory;
	}

	public LocalInstallDirectoryBuggedException(final String message, final Throwable cause,
			final String buggedDirectory) {
		super(message, cause);
		this.buggedDirectory = buggedDirectory;
	}

	public LocalInstallDirectoryBuggedException(final Throwable cause, final String buggedDirectory) {
		super(cause);
		this.buggedDirectory = buggedDirectory;
	}

}
