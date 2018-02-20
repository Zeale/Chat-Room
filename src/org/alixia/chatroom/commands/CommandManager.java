package org.alixia.chatroom.commands;

import java.util.Arrays;
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

		// Handle args.
		String cmd = input.substring(1, input.indexOf(" "));// 1 gets rid of '/'
		String args = input.substring(input.indexOf(" ") + 1);
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

	/**
	 * Runs a command where the command's name is the first item in the string array
	 * and the command's arguments are the remaining items in the string array.
	 * 
	 * @param args
	 *            The string array containing the command name and the given
	 *            arguments.
	 * @return <code>true</code> if the input command was matched to a command in
	 *         this {@link CommandManager}, false otherwise.
	 */
	public boolean runCommand(String... args) {
		String name;

		// Get the command's name.
		if (args.length == 0)
			name = "";
		else
			name = args[0];

		// Make an args array.
		String[] newArgs = new String[args.length - 1];

		// Populate the new args array.
		for (int i = 1; i < args.length; i++)
			newArgs[i - 1] = args[i];

		return runCommand(name, newArgs);
	}
}
