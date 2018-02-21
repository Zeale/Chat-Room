package org.alixia.chatroom.commands;

public abstract class Command {

	private CommandManager managerRef;

	public void setManager(CommandManager managerRef) {
		this.managerRef = managerRef;
	}
	
	protected final void addConsumer(CommandConsumer consumer) {
		managerRef.pushConsumer(consumer);
	}

	protected abstract boolean match(String name);

	protected abstract void act(String name, String... args);

	/**
	 * Returns <code>true</code> if <code>arg</code> equals any of the possible
	 * matches given.
	 * 
	 * @param arg
	 *            The {@link String} argument to check against all the possible
	 *            matches.
	 * @param possibleMatches
	 *            Possible matches of arg.
	 * @return <code>true</code> if there exists an element in possibleMatches,
	 *         (<code>e</code>), such that
	 * 
	 *         <pre>
	 *         e.equals(arg)
	 *         </pre>
	 * 
	 *         would return <code>true</code>.
	 * @see #equalsAnyIgnoreCase(String, String...)
	 */
	protected final boolean equalsAny(String arg, String... possibleMatches) {
		for (String s : possibleMatches)
			if (arg.equals(s))
				return true;
		return false;
	}

	/**
	 * Returns <code>true</code> if a <code>possibleMatch</code> equals
	 * <code>arg</code> ignoring casing.
	 * 
	 * @param arg
	 *            The argument to check against possible matches.
	 * @param possibleMatches
	 *            The possible matches to the argument.
	 * @return <code>true</code> if there exists an element in possibleMatches,
	 *         (<code>e</code>), such that
	 * 
	 *         <pre>
	 *         e.equalsIgnoreCase(arg)
	 *         </pre>
	 * 
	 *         would return <code>true</code>.
	 * @see #equalsAny(String, String...)
	 */
	protected final boolean equalsAnyIgnoreCase(String arg, String... possibleMatches) {
		for (String s : possibleMatches)
			if (arg.equalsIgnoreCase(s))
				return true;
		return false;
	}

}
