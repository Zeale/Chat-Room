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

	@Override
	public String toString() {
		return verified ? "User found and verified." : "The user could not be verified.";
	}

}
