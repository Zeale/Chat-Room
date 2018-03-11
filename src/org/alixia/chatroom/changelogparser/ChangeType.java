package org.alixia.chatroom.changelogparser;

public enum ChangeType {
	ADDITION, CHANGE, DELETION, FIX;

	public static ChangeType valueOfChar(final String changePrecedent) {
		switch (changePrecedent.toLowerCase()) {
		case "+":
			return ChangeType.ADDITION;
		case "~":
			return ChangeType.CHANGE;
		case "-":
			return ChangeType.DELETION;
		case "•":
			return ChangeType.FIX;
		default:
			return null;
		}
	}

	public String toChar() {
		switch (this) {
		case ADDITION:
			return "+";
		case CHANGE:
			return "~";
		case DELETION:
			return "-";
		case FIX:
			return "•";
		default:
			return null;
		}
	}
}
