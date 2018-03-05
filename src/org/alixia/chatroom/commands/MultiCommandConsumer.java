package org.alixia.chatroom.commands;

import java.util.LinkedList;
import java.util.List;

public class MultiCommandConsumer extends CommandConsumer {

	private final List<Command> commands = new LinkedList<>();

	public void addCommands(Command... commands) {
		for (Command c : commands)
			if (!this.commands.contains(c))
				this.commands.add(c);
	}

	@Override
	public void consume(String command, String... args) {
		for (Command c : commands)
			if (c.match(command)) {
				c.act(command, args);
				return;
			}
	}

}
