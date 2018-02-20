package org.alixia.chatroom.connections;

import java.io.IOException;

public class ServerManager extends NamedObjectList<Server> {
	@Override
	public boolean removeItem(String name) {
		Server s = items.remove(name);
		if (s == null)
			return false;
		try {
			s.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void close() {
		for (Server s : items.values())
			try {
				s.stop();
			} catch (IOException e) {
				e.printStackTrace();
			}
		items.clear();
	}
}
