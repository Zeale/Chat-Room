package org.alixia.chatroom.api.changelogparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.Printable;
import org.alixia.chatroom.api.Version;

import javafx.scene.paint.Color;

public class ChangelogParser {

	private final InputStreamReader reader;
	private String updateName;
	private Version version;

	public ChangelogParser(final InputStream stream) {
		reader = new InputStreamReader(stream);
	}

	public ChangelogParser(final String absolutePath) {
		reader = new InputStreamReader(getClass().getResourceAsStream(absolutePath));
	}

	public boolean hasHeader() {
		return updateName != null;
	}

	/**
	 * Gets the next change. This method returns <code>null</code> if there are no
	 * more changes.
	 *
	 * @return The next change in the change log as a {@link Change} object, or
	 *         <code>null</code> if there are no more changes.
	 */
	public Change getNextChange() {
		try {

			String change = "";
			int c;
			ChangeType type;

			// Checks for whitespace
			while (Character.isWhitespace(c = reader.read()))
				;
			if (c == -1)
				return null;
			// Leaves us on the first change type character.

			// For this version, each change type is only represented by a single character.
			// This means that whatever character we're on after we've cleared out
			// whitespace is our change type character, (either +, -, or ~).
			type = ChangeType.valueOfChar("" + (char) c);
			if (type == null)
				throw new ParseException("The type of a change could not be ascertained.");

			while (reader.ready()) {
				c = reader.read();
				if (c == -1 || !("" + (char) c).matches("."))
					return new Change(type, change);
				change += (char) c;
			}

			return new Change(type, change);

		} catch (final Exception e) {
			throw new ParseException(e);
		}

	}

	public String getUpdateName() {
		if (updateName == null)
			parseUpdateHeader();
		return updateName;
	}

	private void parseUpdateHeader() {
		if (updateName != null)
			return;
		else
			try {
				reader.reset();
			} catch (final IOException e1) {
				System.err.println(
						"Failed to reset a ChangeLogParser stream. This shouldn't be an issue if this is the first time this parser is being used.");
			}

		int c;
		String header = "", buffer = "";
		try {
			// Ignore any trailing whitespace.
			while (Character.isWhitespace(c = reader.read()))
				;
			// If we reached the end of the stream, throw an exception since we haven't
			// finished parsing the header.
			if (c == -1)
				throw new ParseException("End of file reached before starting reading of header.");

			// We are at the first character of the update's title.
			String version = "" + (header += (char) c);
			boolean foundColon = false, versionTerminated = false;
			while (true) {
				// Get next char.
				c = reader.read();

				if (!versionTerminated)
					if (!(Character.isDigit(c) || c == '.' || Character.isLetter(c) || c == '-'))
						versionTerminated = true;
					else
						version += (char) c;

				if (c == -1)// End of file
					throw new ParseException(
							"End of file reached before header was terminated (with a colon and a line feed).");
				// If last char was colon
				if (foundColon) {
					if (c == '\r') {
						buffer = "\r";
						continue;
					}

					// Detect line feeds
					if (!(buffer + (char) c).matches("."))
						break;
					else
						// Add the colon since it is part of the name and isn't terminating the header.
						header += ':';
				} else if (!String.valueOf((char) c).matches("."))
					throw new ParseException("Line feed detected before a colon.");
				// Reset the buffer and whether or not a colon was found.
				buffer = "";
				foundColon = false;
				// Detect colons
				if (c == ':') {
					foundColon = true;
					continue;
				}
				// Add the character.
				header += (char) c;
			}
			if (header.isEmpty())
				throw new ParseException("Empty header");
			updateName = header;
			System.out.println(version);
			this.version = new Version(version);

		} catch (final IOException e) {
			throw new ParseException(e);

		}
	}

	public Version getVersion() {
		if (!hasHeader())
			parseUpdateHeader();
		return version;
	}

	public void printChangelog(final Printable printable) {
		printable.print("Version: ", Color.MEDIUMAQUAMARINE);
		printable.println(getUpdateName(), Color.WHITE);
		printable.println();
		Change change;
		int i = 0;
		while ((change = getNextChange()) != null) {
			printable.print("" + ++i, Color.ORANGE);
			printable.print(change.type.toChar() + " ", Color.WHITE);
			if (change.type == ChangeType.ADDITION)
				printable.println(change.text, ChatRoom.SUCCESS_COLOR);
			else if (change.type == ChangeType.CHANGE)
				printable.println(change.text, Color.ORANGE);
			else if (change.type == ChangeType.DELETION)
				printable.println(change.text, ChatRoom.ERROR_COLOR);
			else if (change.type == ChangeType.FIX)
				printable.println(change.text, Color.CORNFLOWERBLUE);
			else
				printable.println(change.text, Color.DARKSALMON);
		}
		printable.println();
	}

}
