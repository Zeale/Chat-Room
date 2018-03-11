package org.alixia.chatroom.commands;

import java.util.LinkedList;
import java.util.List;

public class MultiCommandConsumer extends CommandConsumer {

	private final List<Command> commands = new LinkedList<>();

	public void addCommands(final Command... commands) {
		for (final Command c : commands)
			if (!this.commands.contains(c))
				this.commands.add(c);
	}

	@Override
	public void consume(final String command, final String... args) {
		for (final Command c : commands)
			if (c.match(command)) {
				c.act(command, args);
				return;
			}
	}

}
