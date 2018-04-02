package org.alixia.chatroom.api;

import java.util.Collection;
import java.util.LinkedList;

public class QuickList<E> extends LinkedList<E> {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	@SafeVarargs
	public QuickList(E... es) {
		for (E e : es)
			add(e);
	}

	public QuickList(Collection<? extends E> c) {
		super(c);
	}

}
