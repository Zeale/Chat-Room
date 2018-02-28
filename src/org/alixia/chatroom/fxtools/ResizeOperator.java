package org.alixia.chatroom.fxtools;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

public class ResizeOperator {

	private final Region region;
	private int resizeMargin = 8;

	private boolean dragging;
	private boolean x, y;

	public ResizeOperator(Region region, int resizeMargin) {
		this(region);
		this.resizeMargin = resizeMargin;
	}

	public ResizeOperator(Region region) {
		this.region = region;
		setup();
	}

	private boolean inRange(MouseEvent event) {
		return event.getY() <= resizeMargin || event.getY() >= region.getHeight() - resizeMargin// Top or bottom edges
				|| event.getX() <= resizeMargin || event.getX() >= region.getWidth() - resizeMargin;// Left or right
																									// edges
	}

	private void setup() {

		region.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (event.getY() <= resizeMargin)
					if (event.getX() < resizeMargin)
						region.setCursor(Cursor.NW_RESIZE);
					else if (event.getX() >= region.getWidth() - resizeMargin)
						region.setCursor(Cursor.NE_RESIZE);
					else
						region.setCursor(Cursor.N_RESIZE);
				else if (event.getY() >= region.getHeight() - resizeMargin)
					if (event.getX() < resizeMargin)
						region.setCursor(Cursor.SW_RESIZE);
					else if (event.getX() >= region.getWidth() - resizeMargin)
						region.setCursor(Cursor.SE_RESIZE);
					else
						region.setCursor(Cursor.S_RESIZE);
				else if (event.getX() <= resizeMargin)
					region.setCursor(Cursor.E_RESIZE);
				else if (event.getX() >= region.getWidth() - resizeMargin)
					region.setCursor(Cursor.W_RESIZE);
				else
					region.setCursor(Cursor.DEFAULT);
			}
		});
		region.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (inRange(event)) {
					dragging = true;

				}
			}
		});

	}

}
