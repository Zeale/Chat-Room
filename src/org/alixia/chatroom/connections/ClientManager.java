package org.alixia.chatroom.connections;

public class ClientManager extends NamedObjectList<Client> {
	@Override
	public boolean removeItem(String name) {
		Client c = items.remove(name);
		if (c == null)
			return false;
		c.closeConnection();
		return true;
	}

	@Override
	public boolean addItem(Client client) {
		client.setPaused(true);
		return super.addItem(client);
	}

	@Override
	public boolean selectItem(String clientName) {
		if (!containsKey(clientName))
			return false;
		if (isItemSelected())
			getSelectedItem().setPaused(true);

		items.get(clientName).setPaused(false);
		return super.selectItem(clientName);
	}

	@Override
	public void close() {
		for (Client c : items.values())
			c.closeConnection();
		items.clear();
	}
}
