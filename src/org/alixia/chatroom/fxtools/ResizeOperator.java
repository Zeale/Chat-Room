package org.alixia.chatroom.fxtools;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

public class ResizeOperator {

	private final Region region;
	private int resizeMargin = 8;

	private final Resizable mover;

	private boolean dragging;
	private boolean x, y;

	public ResizeOperator(Region region, Resizable mover, int resizeMargin) {
		this(region, mover);
		this.resizeMargin = resizeMargin;
	}

	public ResizeOperator(Region region, Resizable mover) {
		this.region = region;
		this.mover = mover;
		setup();
	}

	private boolean inRange(MouseEvent event) {
		return top(event) || bottom(event) || left(event) || right(event);
	}

	public boolean left(MouseEvent e) {
		return e.getX() <= resizeMargin;
	}

	public boolean right(MouseEvent e) {
		return e.getX() >= region.getWidth() - resizeMargin;
	}

	public boolean top(MouseEvent e) {
		return e.getY() <= resizeMargin;
	}

	public boolean bottom(MouseEvent e) {
		return e.getY() >= region.getHeight() - resizeMargin;
	}

	public boolean topright(MouseEvent e) {
		return top(e) && right(e);
	}

	public boolean bottomright(MouseEvent e) {
		return bottom(e) && right(e);
	}

	public boolean topleft(MouseEvent e) {
		return top(e) && left(e);
	}

	public boolean bottomleft(MouseEvent e) {
		return bottom(e) && left(e);
	}

	private void updateTop(MouseEvent event) {
		// They are dragging the top up and down. Lock the bottom, left, and right.
		mover.moveY(event.getY());// They are dragging out of the node, so move downwards by y. (y is negative
									// since they are going up. Up is negative.) This moves the Resizable upwards.
		mover.expandVer(-event.getY());// Expand it by how much they moved, or shrink it by the amount.

		// If the user drags upwards, (meaning they want to enlarge the item), then this
		// method will be called and it will move the item upwards, while expanding it
		// by the same amount vertically. This simulates the bottom being anchored.
		//
		// (I have no idea if this will look jittery or not.)
	}

	private void updateBottom(MouseEvent e) {
		mover.expandVer(e.getY());
	}

	private void setup() {

		region.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (top(event))
					region.setCursor(
							left(event) ? Cursor.NW_RESIZE : (right(event) ? Cursor.NE_RESIZE : Cursor.N_RESIZE));
				else if (bottom(event))
					region.setCursor(
							left(event) ? Cursor.SW_RESIZE : (right(event) ? Cursor.SE_RESIZE : Cursor.S_RESIZE));
				else
					region.setCursor(left(event) ? Cursor.W_RESIZE : (right(event) ? Cursor.E_RESIZE : Cursor.DEFAULT));
			}
		});
		region.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (inRange(event)) {
					dragging = true;
					event.consume();
				}
			}
		});

		region.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (dragging) {
					if (top(event))
						updateTop(event);
					if (bottom(event))
						updateBottom(event);
					event.consume();
				}

			}
		});

	}

}
