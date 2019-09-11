/**
 * 
 */
package debug.commandConsole;

import java.util.LinkedList;

/**
 * @author timat
 *
 */
public abstract class ConsoleCommand {

	public static final LinkedList<ConsoleCommand> ALL_COMMANDS = new LinkedList<ConsoleCommand>();

	/**
	 * 
	 */
	protected ConsoleCommand() {
		ALL_COMMANDS.add(this);
	}

	/**
	 * This method will be invoked when this command will be determinated.
	 * 
	 * @param args Array of strings with parameters but first one must be the command determinant.
	 */
	public abstract void analize(String[] args);

	public abstract String getDeterminant();
	
}
