package org.alixia.chatroom.fxtools;

public interface Resizable {
	void expandHor(double amount);

	void expandVer(double amount);

	double getX();

	double getY();

	void moveX(double amount);

	void moveY(double amount);
}
