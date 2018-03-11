package org.alixia.chatroom.changelogparser;

public class Change {

	public final ChangeType type;
	public final String text;

	public Change(final ChangeType type, final String text) {
		this.type = type;
		this.text = text;
	}

}
