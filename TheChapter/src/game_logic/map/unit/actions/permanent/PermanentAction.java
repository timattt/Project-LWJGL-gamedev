package game_logic.map.unit.actions.permanent;

import game_logic.map.unit.Unit;
import game_logic.map.unit.actions.UnitAction;

public abstract class PermanentAction {

	protected Unit unit;

	private boolean started = false;

	protected abstract void invoked();

	public abstract void interrupted();

	public abstract void nextTurn();

	public abstract void nextTeamTurn();

	public abstract void updateGraphics();

	public abstract void init(Object... params);
	
	public abstract boolean isSubsidiary(Class<? extends UnitAction> action);
	
	public final void init(Unit unit) {
		this.unit = unit;
	}

	public final void start() {
		started = true;
		invoked();
	}

	public final boolean isStarted() {
		return started;
	}

}
