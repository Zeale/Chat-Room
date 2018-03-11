package org.alixia.chatroom.commands;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class CommandManager {
	private final List<Command> commands = new LinkedList<>();

	private final Stack<CommandConsumer> consumers = new Stack<>();

	private String commandChar = "/";

	public CommandManager() {
	}

	public CommandManager(final String commandChar) {
		this.commandChar = commandChar;
	}

	public void addCommand(final Command command) {
		if (commands.contains(command))
			return;
		command.setManager(this);
		commands.add(command);
	}

	public String getCommandChar() {
		return commandChar;
	}

	public boolean hasConsumer() {
		return !consumers.isEmpty();
	}

	void pushConsumer(final CommandConsumer consumer) {
		// Stack can have dupes, in case consumption is wanted twice. Command list can't
		// have dupes.
		consumers.push(consumer);
	}

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
	public boolean runCommand(final String rawInput) {

		String input = rawInput.trim();

		// Get rid of duplicate spaces.
		while (input.contains("  "))
			input = input.replaceAll("  ", " ");

		// Handle no args.
		if (!input.contains(" "))
			if (input.isEmpty())
				return runCommand("", new String[0]);
			else
				return runCommand(input.substring(hasConsumer() ? 0 : commandChar.length()), new String[0]);

		// Handle args.
		final String cmd = input.substring(hasConsumer() ? 0 : commandChar.length(), input.indexOf(" "));

		final String args = input.substring(input.indexOf(" ") + 1);
		final String[] argArr = args.split(" ");

		if (hasConsumer()) {
			consumers.pop().consume(cmd, args);
			return true;
		}
		return runCommand(cmd, argArr);

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
	public boolean runCommand(final String... args) {

		String name;

		// Get the command's name.
		if (args.length == 0)
			name = "";
		else
			name = args[0];

		// Make an args array.
		final String[] newArgs = new String[args.length - 1];

		// Populate the new args array.
		for (int i = 1; i < args.length; i++)
			newArgs[i - 1] = args[i];

		return runCommand(name, newArgs);
	}

	public boolean runCommand(final String cmd, final String... args) {
		if (!consumers.isEmpty()) {
			consumers.pop().consume(cmd, args);
			return true;
		}
		for (final Command c : commands)
			if (c.match(cmd)) {
				c.act(cmd, args);
				return true;
			}
		return false;
	}

	public void setCommandChar(final String commandChar) {
		this.commandChar = commandChar;
	}
}
