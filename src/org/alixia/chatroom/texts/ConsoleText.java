package org.alixia.chatroom.texts;

import org.alixia.chatroom.api.Console;

import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public abstract class ConsoleText {

	protected final static String DEFAULT_FAMLIY = Font.getDefault().getFamily();
	protected final static double DEFAULT_SIZE = 16;
	protected static final Font DEFAULT_FONT = Font.font(DEFAULT_FAMLIY, DEFAULT_SIZE);

	protected final void formatText(Text... texts) {
		for (Text t : texts)
			t.setFont(DEFAULT_FONT);
	}

	/**
	 * Returns a formatted linebreak {@link Text} object.
	 * 
	 * @return A {@link Text} that has been formatted and contains only the
	 *         {@link String}: <code>\n</code>.
	 */
	protected final Text println() {
		Text linebreak = new Text("\n");
		formatText(linebreak);
		return linebreak;
	}

	@Deprecated
	public abstract void print(TextFlow flow);

	public abstract void print(Console console);

}
