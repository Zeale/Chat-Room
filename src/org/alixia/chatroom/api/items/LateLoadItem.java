package org.alixia.chatroom.api.items;

import org.alixia.chatroom.api.ItemConstructor;

/**
 * Wraps an object that gets constructed once its used. An
 * {@link ItemConstructor} (which is specified in the constructor) is given and
 * is used to construct what this object wraps. The wrapped object is
 * constructed and returned the first time {@link #get()} is called. From then
 * on, it is simply returned.
 *
 * @author Zeale
 *
 * @param <T>
 */
public class LateLoadItem<T> implements Getable<T> {

	private boolean constructed;
	private T item;
	private final ItemConstructor<T> constructor;

	public LateLoadItem(final ItemConstructor<T> constructor) {
		this.constructor = constructor;
	}

	@Override
	public T get() {
		if (!constructed) {
			item = constructor.construct();
			constructed = true;
		}
		return item;
	}

}
