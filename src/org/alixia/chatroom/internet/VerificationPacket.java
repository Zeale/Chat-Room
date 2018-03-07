package org.alixia.chatroom.internet;

import java.io.Serializable;

public class VerificationPacket implements Serializable {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public final boolean verified;

	public VerificationPacket(boolean verified) {
		this.verified = verified;
	}
	
	

}
