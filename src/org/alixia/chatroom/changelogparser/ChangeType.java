package org.alixia.chatroom.changelogparser;

public enum ChangeType {
	ADDITION, CHANGE, DELETION;

	public static ChangeType valueOfChar(String changePrecedent) {
		switch (changePrecedent.toLowerCase()) {
		case "+":
			return ChangeType.ADDITION;
		case "~":
			return ChangeType.CHANGE;
		case "-":
			return ChangeType.DELETION;
		default:
			return null;
		}
	}
}
