package org.alixia.chatroom.connections;

public class ClientManager extends NamedObjectList<Client> {

	private final ConnectionListener listener;

	public ClientManager(final ConnectionListener clientListener) {
		listener = clientListener;
	}

	@Override
	public boolean addItem(final Client client) {
		client.setPaused(true);
		return super.addItem(client);
	}

	@Override
	public void close() {
		for (final Client c : items.values())
			c.closeConnection();
		items.clear();
	}

	@Override
	public boolean removeItem(final String name) {
		final Client c = items.remove(name);
		if (c == null)
			return false;
		c.closeConnection();
		return true;
	}

	@Override
	public boolean selectItem(final String clientName) {
		if (!containsKey(clientName))
			return false;
		if (isItemSelected())
			getSelectedItem().setListener(null);

		items.get(clientName).setListener(listener);
		return super.selectItem(clientName);
	}
}
