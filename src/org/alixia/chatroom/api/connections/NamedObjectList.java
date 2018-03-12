package org.alixia.chatroom.api.connections;

import java.util.Collection;
import java.util.HashMap;

abstract class NamedObjectList<T extends NamedObject> {

	protected final HashMap<String, T> items = new HashMap<>();

	private T selectedItem;

	public boolean addItem(final T client) {
		// putIfAbsent returns null if there was no mapping, so it returns null if we
		// successfully added an item.
		return items.putIfAbsent(client.getName(), client) == null;
	}

	public abstract void close();

	public boolean containsKey(final String name) {
		return items.containsKey(name);
	}

	public boolean containsValue(final T value) {
		return items.containsValue(value);
	}

	public T getItem(final String name) {
		return items.get(name);
	}

	public T getSelectedItem() {
		return selectedItem;
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	public boolean isItemSelected() {
		return getSelectedItem() != null;
	}

	protected final T rawGetSelectedItem() {
		return selectedItem;
	}

	protected final void rawSetSelectedItem(final T item) {
		selectedItem = item;
	}

	/**
	 * Removes a client, given its name. The client is closed.
	 *
	 * @param name
	 *            The name of the {@link Client}.
	 * @return <code>true</code> if a {@link Client} existed with the specified name
	 *         and was successfully removed. <code>false</code> otherwise.
	 */
	public boolean removeItem(final String name) {
		return items.remove(name) != null;
	}

	public boolean selectItem(final String clientName) {
		if (items.containsKey(clientName)) {
			rawSetSelectedItem(items.get(clientName));
			return true;
		}
		return false;
	}

	public void unselectItem() {
		rawSetSelectedItem(null);
	}

	public Collection<T> values() {
		return items.values();
	}

}
