package org.alixia.chatroom.commands;

import java.util.LinkedList;
import java.util.List;

public class CommandManager {
	public final List<Command> commands = new LinkedList<>();

	/**
	 * This method operates assuming the precondition that the first character of
	 * the trimmed value of <code>rawInput</code> is "<code>/</code>". If such is
	 * not the case, this method will improperly attempt to match the input with
	 * commands' names, and, as a result, call those commands.
	 * 
	 * @param rawInput
	 *            The user's command, with the arguments as one whole string. This
	 *            should start with "<code>/</code>".
	 */
	public boolean runCommand(String rawInput) {

		String input = rawInput.trim();

		// Get rid of duplicate spaces.
		while (input.contains("  "))
			input = input.replaceAll("  ", " ");

		// Handle no args.
		if (!input.contains(" "))
			return runCommand(input.substring(1), new String[0]);

		String cmd = input.substring(1, input.indexOf(" "));
		String args = input.substring(input.indexOf(" "));
		String[] argArr = args.split(" ");

		return runCommand(cmd, argArr);

	}

	public boolean runCommand(String cmd, String... args) {
		for (Command c : commands)
			if (c.match(cmd)) {
				c.act(cmd, args);
				return true;
			}
		return false;
	}
}
