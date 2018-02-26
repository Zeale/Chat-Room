package org.alixia.chatroom.changelogparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ChangelogParser {

	private final InputStreamReader reader;
	private String updateName;

	public ChangelogParser(InputStream stream) {
		reader = new InputStreamReader(stream);
	}

	public ChangelogParser(String absolutePath) {
		reader = new InputStreamReader(getClass().getResourceAsStream(absolutePath));
	}

	private void parseUpdateHeader() {
		if (updateName != null)
			return;
		else
			try {
				reader.reset();
			} catch (IOException e1) {
			}

		int c;
		String header = "";
		String buffer = "";
		try {
			// Ignore any trailing whitespace.
			while (Character.isWhitespace(c = reader.read()))
				;
			// If we reached the end of the stream, throw an exception since we haven't
			// finished parsing the header.
			if (c == -1)
				throw new ParseException("End of file reached before starting reading of header.");

			// We are at the first character of the update's title.
			header += (char) c;
			boolean foundColon = false;
			while (true) {
				// Get next char.
				c = reader.read();
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
					else {
						// Add the colon since it is part of the name and isn't terminating the header.
						header += ':';
					}
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
			this.updateName = header;

		} catch (IOException e) {
			throw new ParseException(e);
		}
	}

	public String getUpdateName() {
		if (updateName == null) {
			parseUpdateHeader();
		}
		return updateName;
	}

}
