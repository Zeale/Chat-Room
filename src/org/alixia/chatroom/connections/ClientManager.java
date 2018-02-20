package org.alixia.chatroom.connections;

public class ClientManager extends SelectableObjectManager<Client> {
	@Override
	public boolean removeItem(String name) {
		Client c = items.remove(name);
		if (c == null)
			return false;
		c.closeConnection();
		return true;
	}

	@Override
	public void close() {
		for (Client c : items.values())
			c.closeConnection();
		items.clear();
	}
}
