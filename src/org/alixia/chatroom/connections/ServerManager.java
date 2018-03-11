package org.alixia.chatroom.connections;

import java.io.IOException;

public class ServerManager extends NamedObjectList<Server> {
	@Override
	public void close() {
		for (final Server s : items.values())
			try {
				s.stop();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		items.clear();
	}

	@Override
	public boolean removeItem(final String name) {
		final Server s = items.remove(name);
		if (s == null)
			return false;
		try {
			s.stop();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return true;
	}
}
