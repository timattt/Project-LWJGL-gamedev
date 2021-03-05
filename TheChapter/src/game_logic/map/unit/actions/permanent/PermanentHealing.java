package game_logic.map.unit.actions.permanent;

import game_logic.map.unit.actions.Healing;
import game_logic.map.unit.actions.UnitAction;

public class PermanentHealing extends PermanentAction {

	// Heal value
	private float val;

	@Override
	public void invoked() {
		if (!(unit.hasFullMovePoints() && unit.getCurrentHealth() != 1f)) {
			unit.interruptPermanentAction();
		}
	}

	@Override
	public void interrupted() {
	}

	@Override
	public void nextTurn() {
		if (unit.hasFullMovePoints() && unit.getCurrentHealth() != 1f) {
			unit.heal(val);
		} else {
			unit.interruptPermanentAction();
		}
	}

	@Override
	public void nextTeamTurn() {
		
	}

	@Override
	public void updateGraphics() {
		if (!(unit.hasFullMovePoints() && unit.getCurrentHealth() != 1f)) {
			unit.interruptPermanentAction();
		}
	}

	@Override
	public void init(Object... params) {
		val = (float) params[0];
	}

	@Override
	public boolean isSubsidiary(Class<? extends UnitAction> action) {
		return action == Healing.class;
	}

}
