package org.alixia.chatroom.api.connections;

import java.io.Serializable;

public interface ConnectionListener {
	void connectionClosed();

	void objectReceived(Serializable object);
}
