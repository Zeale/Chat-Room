package org.alixia.chatroom.connections;

import java.util.HashMap;

abstract class SelectableObjectManager<T> {

	protected final HashMap<String, T> items = new HashMap<>();

	private T selectedItem;

	public T getSelectedItem() {
		return selectedItem;
	}

	public boolean isItemSelected() {
		return selectedItem != null;
	}

	/**
	 * Removes a client, given its name. The client is closed.
	 * 
	 * @param name
	 *            The name of the {@link Client}.
	 * @return <code>true</code> if a {@link Client} existed with the specified name
	 *         and was successfully removed. <code>false</code> otherwise.
	 */
	public boolean removeItem(String name) {
		return items.remove(name) != null;
	}

	public boolean selectItem(String clientName) {
		if (items.containsKey(clientName)) {
			selectedItem = items.get(clientName);
			return true;
		}
		return false;
	}

	public void unselectItem() {
		selectedItem = null;
	}

	public boolean addItem(String name, T client) {
		// putIfAbsent returns null if there was no mapping, so it returns null if we
		// successfully added an item.
		return items.putIfAbsent(name, client) == null;
	}

	public abstract void close();

}
