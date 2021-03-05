package game_logic;

import java.util.LinkedList;

public abstract class Executor {

	// Mesh to load
	private static final LinkedList<Executor> loaders = new LinkedList<Executor>();
	
	// Loaded
	private boolean executed;

	public boolean isExecuted() {
		return executed;
	}

	public void setExecuted(boolean ex) {
		executed = ex;
	}

	public abstract void executed();

	public void waitUntilLoaded() {
		while (!executed) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public static final void updateExecutor() {
		while (!loaders.isEmpty()) {
			loaders.getFirst().executed();
			loaders.removeFirst().setExecuted(true);
		}
	}
	
	public static final void execute(Executor ml) {
		loaders.addLast(ml);
	}

}
