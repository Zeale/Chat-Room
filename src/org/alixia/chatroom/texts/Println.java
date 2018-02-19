package org.alixia.chatroom.texts;

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

	@Override
	public void print(TextFlow flow) {
		flow.getChildren().add(println());
	}

	/**
	 * Prints a linebreak to the specified {@link TextFlow}. The linebreak is
	 * formatted.
	 * 
	 * @param flow
	 *            The {@link TextFlow} to print a linebreak to.
	 */
	public Println(TextFlow flow) {
		print(flow);
	}

}
