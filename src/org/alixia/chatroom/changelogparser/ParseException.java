package org.alixia.chatroom.changelogparser;

public class ParseException extends RuntimeException {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public ParseException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ParseException(final String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ParseException(final String message, final Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ParseException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public ParseException(final Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
