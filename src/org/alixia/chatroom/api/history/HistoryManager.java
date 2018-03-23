package org.alixia.chatroom.api.history;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
	private final List<String> history = new ArrayList<>();
	{
		// This adds the "" string to the history list.
		clear();
	}

	public void clear() {
		history.clear();
		history.add("");
	}

	private int pos = 0;

	public void send(String text) {
		if (!text.equals(getLatest()))
			history.add(text);
		reset();
	}

	public void reset() {
		pos = history.size();
	}

	public String getLatest() {
		return history.get(history.size() - 1);
	}

	public String getCurrent() {
		if (pos == history.size())
			return null;
		return history.get(pos);
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

}
