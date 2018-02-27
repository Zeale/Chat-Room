package org.alixia.chatroom.connections;

public class ClientManager extends NamedObjectList<Client> {

	private final ConnectionListener listener;

	public ClientManager(ConnectionListener clientListener) {
		listener = clientListener;
	}

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
			getSelectedItem().setListener(null);

		items.get(clientName).setListener(listener);
		return super.selectItem(clientName);
	}

	@Override
	public void close() {
		for (Client c : items.values())
			c.closeConnection();
		items.clear();
	}
}
