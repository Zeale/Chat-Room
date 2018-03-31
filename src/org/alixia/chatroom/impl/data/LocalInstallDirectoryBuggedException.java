package org.alixia.chatroom.impl.data;

public class LocalInstallDirectoryBuggedException extends RuntimeException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public LocalInstallDirectoryBuggedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace, String buggedDirectory) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.buggedDirectory = buggedDirectory;
	}

	public LocalInstallDirectoryBuggedException(String message, Throwable cause, String buggedDirectory) {
		super(message, cause);
		this.buggedDirectory = buggedDirectory;
	}

	public LocalInstallDirectoryBuggedException(Throwable cause, String buggedDirectory) {
		super(cause);
		this.buggedDirectory = buggedDirectory;
	}

	public LocalInstallDirectoryBuggedException(String buggedDirectory) {
		this.buggedDirectory = buggedDirectory;
	}

	public LocalInstallDirectoryBuggedException(String message, String buggedDirectory) {
		super(message);
		this.buggedDirectory = buggedDirectory;
	}

	public final String buggedDirectory;

}
