package org.alixia.chatroom.api.fx.nodes;

import org.alixia.chatroom.api.Printable;
import org.alixia.chatroom.api.QuickList;

import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;

public class HelpButton extends InfoButton<Text> implements org.alixia.chatroom.api.Console, Printable {

	public static final Paint SIMPLE_BLUE = new RadialGradient(0, 0, 16, 0, 8, false, CycleMethod.NO_CYCLE,
			new QuickList<>(new Stop(0, Color.WHITE), new Stop(0.2, Color.SLATEBLUE)));

	private final Text questionMark = new Text("?");

	public HelpButton() {
	}

	private final HelpWindow helpWindow = new HelpWindow();

	public HelpWindow getHelpWindow() {
		return helpWindow;
	}

	{
		setGraphic(questionMark);
	}

	@Override
	protected void onClick(MouseEvent event) {
		if (getHelpWindow().isShowing())
			getHelpWindow().hide();
		else
			getHelpWindow().show();
	}

	@Override
	public void print(String text, Color color) {
		helpWindow.print(text, color);
	}

	@Override
	public void printText(Text text) {
		helpWindow.printText(text);
	}

	public void printBullet() {
		print(" " + (char) 8226 + "  ", Color.WHITE);
	}

	public void printBullet(String text, Color color) {
		printBulletPart(text, color);
		println();
	}

	public void printBulletPart(String text, Color color) {
		printBullet();
		print(text, color);
	}

}
