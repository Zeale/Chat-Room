package org.alixia.chatroom.connections;

import java.util.Collection;
import java.util.HashMap;

abstract class NamedObjectList<T extends NamedObject> {

	protected final HashMap<String, T> items = new HashMap<>();

	private T selectedItem;

	protected final void rawSetSelectedItem(T item) {
		selectedItem = item;
	}

	protected final T rawGetSelectedItem() {
		return selectedItem;
	}

	public T getSelectedItem() {
		return selectedItem;
	}
	
	public Collection<T> values(){
		return items.values();
	}

	public T getItem(String name) {
		return items.get(name);
	}

	public boolean isItemSelected() {
		return getSelectedItem() != null;
	}

	public boolean containsKey(String name) {
		return items.containsKey(name);
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	public boolean containsValue(T value) {
		return items.containsValue(value);
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
			rawSetSelectedItem(items.get(clientName));
			return true;
		}
		return false;
	}

	public void unselectItem() {
		rawSetSelectedItem(null);
	}

	public boolean addItem(T client) {
		// putIfAbsent returns null if there was no mapping, so it returns null if we
		// successfully added an item.
		return items.putIfAbsent(client.getName(), client) == null;
	}

	public abstract void close();

}
