package org.alixia.chatroom.api.history;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
	private final List<String> history = new ArrayList<>();
	{
		// This adds the "" string to the history list.
		clear();
	}

	private int pos = 0;

	public void clear() {
		history.clear();
		history.add("");
	}

	public String getCurrent() {
		if (pos == history.size())
			return null;
		return history.get(pos);
	}

	public String getLatest() {
		return history.get(history.size() - 1);
	}

	public String getNewer() {
		if (pos + 1 < history.size())
			pos++;
		return getCurrent();
	}

	public String getOlder() {
		if (pos - 1 > -1)
			pos--;
		return getCurrent();

	}

	public void reset() {
		pos = history.size();
	}

	public void send(final String text) {
		if (!text.equals(getLatest()))
			history.add(text);
		reset();
	}

}
