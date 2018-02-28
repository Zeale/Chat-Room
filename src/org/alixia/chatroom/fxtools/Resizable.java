package org.alixia.chatroom.fxtools;

public interface Resizable {
	void moveX(double amount);

	void moveY(double amount);

	double getX();

	double getY();

	void expandHor(double amount);

	void expandVer(double amount);
}
