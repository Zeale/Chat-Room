package org.alixia.chatroom.api.changelogparser;

public class Change {

	public final ChangeType type;
	public final String text;

	public Change(final ChangeType type, final String text) {
		this.type = type;
		this.text = text;
	}

}
