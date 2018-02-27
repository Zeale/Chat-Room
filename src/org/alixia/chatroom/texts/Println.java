package org.alixia.chatroom.texts;

import org.alixia.chatroom.api.Console;

import javafx.scene.text.TextFlow;

/**
 * A quick, convenient {@link ConsoleText} for printing a linebreak into a
 * {@link TextFlow}. Instances can be reused by the {@link #print(TextFlow)}
 * method, but this class will automatically print a linebreak once, when it is
 * created, to the specified {@link TextFlow}.
 * 
 * @author Zeale
 *
 */
public class Println extends ConsoleText {

	/**
	 * Prints a linebreak to the specified {@link TextFlow}. The linebreak is
	 * formatted.
	 * 
	 * @param flow
	 *            The {@link TextFlow} to print a linebreak to.
	 */
	public Println(Console console) {
		print(console);
	}

	@Override
	public void print(Console console) {
		console.printText(println());
	}

}
