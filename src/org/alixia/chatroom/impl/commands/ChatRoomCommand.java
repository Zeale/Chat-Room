package org.alixia.chatroom.impl.commands;

import static org.alixia.chatroom.ChatRoom.INFO_COLOR;
import static org.alixia.chatroom.ChatRoom.SUCCESS_COLOR;

import java.util.LinkedList;
import java.util.List;

import org.alixia.chatroom.ChatRoom;
import org.alixia.chatroom.api.Printable;
import org.alixia.chatroom.api.commands.Command;

import javafx.scene.paint.Color;

abstract class ChatRoomCommand extends Command {

	private final static String[] bulletingOrder = { "-", "+", "~", "•", "" + (char) 9830 };

	{
		// This is basically the only line that makes this class "belong" in the impl
		// package. Otherwise, it's an API class.
		//
		// I suppose this class could be split up into an API class (that goes somewhere
		// else) and this implementation class (which would extend the API class) but
		// they'd have such small amounts of code that it'd be pointless.
		ChatRoom.INSTANCE.commandManager.addCommand(this);
	}

	private int subcmdLevel = 1;

	protected void decSubcmdLevel() {
		subcmdLevel--;
	}

	protected int getSubcmdLevel() {
		return subcmdLevel;
	}

	protected void incSubcmdLevel() {
		subcmdLevel++;
	}

	protected void printHelp(final String usage, final String... descriptions) {
		ChatRoom.INSTANCE.print("Usage: " + usage, INFO_COLOR);
		ChatRoom.INSTANCE.print(" - ", Color.WHITE);
		for (final String s : descriptions)
			ChatRoom.INSTANCE.println(s, SUCCESS_COLOR);
	}

	protected void printHelp(String usage, Subcommand... subcommands) {
		ChatRoom.INSTANCE.println("Usage: " + usage, INFO_COLOR);
		for (Subcommand s : subcommands)
			s.printHelp(1);
	}

	protected void printSubcommandHelp(final String name, final String usage, final String... descriptions) {
		String indentation = "";
		for (int i = 0; i < subcmdLevel; i++)
			indentation += '\t';

		ChatRoom.INSTANCE.print(
				indentation + bulletingOrder[bulletingOrder.length % subcmdLevel] + " Usage: /" + name + " " + usage,
				Color.ORANGE);
		ChatRoom.INSTANCE.print(" - ", Color.ORANGERED);
		for (final String s : descriptions)
			ChatRoom.INSTANCE.println(indentation + s, Color.INDIANRED);
	}

	public final static class Subcommand {

		private static final Color DEFAULT_COLOR = Color.GREEN;

		private final Printable printer = ChatRoom.INSTANCE.printer;

		private final List<Subcommand> subcommands = new LinkedList<>();
		private TextBlock[] description;
		private final String usage;

		public final static class TextBlock {
			public final String text;
			public final Color color;

			public TextBlock(String text, Color color) {
				this.text = text;
				this.color = color;
			}

			public TextBlock(String text) {
				this(text, DEFAULT_COLOR);
			}

		}

		public Subcommand(String usage, String description) {
			this.usage = usage;
			this.description = new TextBlock[] { new TextBlock(description, DEFAULT_COLOR) };
		}

		public Subcommand(String usage, String description, Color color) {
			this.usage = usage;
			this.description = new TextBlock[] { new TextBlock(description, color) };
		}

		public Subcommand(String usage, TextBlock[] description, Subcommand... subcommands) {
			this.usage = usage;
			this.description = description;
			for (Subcommand s : subcommands)
				this.subcommands.add(s);
		}

		public Subcommand(String usage, String description, Subcommand... subcommands) {
			this(usage, description);
			for (Subcommand s : subcommands)
				this.subcommands.add(s);
		}

		public static String getTabs(int count) {
			String s = "";
			for (int i = 0; i < count; i++)
				s += '\t';
			return s;
		}

		public void printHelp(int nestingLevel) {
			printer.print(getTabs(nestingLevel) + "~ ", Color.BLACK);
			printer.print(usage, ChatRoom.INFO_COLOR);
			printer.print(" - ", Color.WHITE);
			for (TextBlock tb : description)
				printer.print(tb.text, tb.color);
			printer.println();
			if (subcommands != null)
				for (Subcommand s : subcommands)
					s.printHelp(nestingLevel + 1);
		}

	}

}
