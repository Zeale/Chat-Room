package org.alixia.chatroom.logging;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.Console;
import org.alixia.chatroom.api.Printable;
import org.alixia.chatroom.texts.BoldText;

import javafx.scene.paint.Color;

public final class Logger {

	public static final Logger CHAT_ROOM_LOGGER = new Logger("CHAT-ROOM");

	public Color bracketColor = Color.ORANGERED, parentColor = Color.MEDIUMVIOLETRED, childColor = Color.RED,
			separatorColor = Color.WHITE, messageColor = Color.WHITE;
	public boolean boldHeader = false;

	protected final Printable printer = ChatRoom.INSTANCE.printer;
	protected final Console console = ChatRoom.INSTANCE.console;
	protected final Printable boldPrinter = (text, color) -> new BoldText(text, color).print(console);

	private final Logger parent;
	private final String name;

	public String separator = ".";

	public Logger(final String name) {
		this.name = name;
		parent = null;
	}

	public Logger(String name, Logger parent) {

		// Handle multiple loggers in one.
		if (name.contains(".")) {

			while (name.contains(".."))
				name = name.replace("..", ".");

			final String[] names = name.split("\\.");

			for (int i = 0; i < names.length - 1; i++) {
				final String n = names[i];
				parent = new Logger(n, parent);
			}

			this.name = names[names.length - 1];
		} else
			this.name = name;
		this.parent = parent;
	}

	public String getFullName() {
		return (parent == null ? "" : parent.getFullName() + ".") + name;
	}

	public String getName() {
		return name;
	}

	public Logger getParent() {
		return parent;
	}

	public String getSeparator() {
		return separator;
	}

	public void log(final String message) {
		printIdentifier();
		printer.println(message, messageColor);
	}

	public void logBold(final String message) {
		printIdentifier();
		new BoldText(message + "\n", messageColor).print(console);

	}

	private void printIdentifier() {
		boldPrinter.print("[", bracketColor);
		final String[] names = getFullName().split("\\.");
		for (int i = 0; i < names.length - 1; i++) {
			final String s = names[i];
			(boldHeader ? boldPrinter : printer).print(s,
					parentColor.interpolate(childColor, (double) i / (names.length - 1)));
			(boldHeader ? boldPrinter : printer).print(separator, separatorColor);
		}
		(boldHeader ? boldPrinter : printer).print(getName(), childColor);
		boldPrinter.print("]: ", bracketColor);

	}

	public void setSeparator(final String separator) {
		this.separator = separator;
	}

}
