package org.alixia.chatroom.connections;

import java.io.Serializable;

public interface ConnectionListener {
	void objectReceived(Serializable object);
	void connectionClosed();
}
