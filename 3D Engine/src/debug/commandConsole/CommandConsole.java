/**
 * 
 */
package debug.commandConsole;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import engine.Engine;
import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorCleanup;
import engine.monoDemeanor.MonoDemeanorInit;
import engine.monoDemeanor.MonoDemeanorInstance;

/**
 * @author timat
 *
 */
@MonoDemeanor
public class CommandConsole implements Runnable {

	@MonoDemeanorInstance
	public static final CommandConsole instance = new CommandConsole();

	private final Thread thread = new Thread(this);
	private boolean isRunning = true;

	/**
	 * 
	 */
	private CommandConsole() {
	}

	@MonoDemeanorInit
	public void init() {
		thread.start();
	}

	@MonoDemeanorCleanup
	public void cleanup() {
		isRunning = false;
	}

	@Override
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while (isRunning && Engine.isRunning()) {
				Thread.sleep(10l);
				if (!br.ready()) {
					continue;
				}
				String line = br.readLine();
				String args[] = line.split(" ");
				for (ConsoleCommand com : ConsoleCommand.ALL_COMMANDS) {
					if (com.getDeterminant().equals(args[0])) {
						com.analize(args);
						break;
					}
				}
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
