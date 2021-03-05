package game_logic.map.unit.actions.combat;

import game_logic.map.unit.actions.UnitAction;

public class Defending extends UnitAction {

	public Defending() {
		super("Defending");
	}
	
	@Override
	public void init(Object... arg) {
		total_action_time = (long) arg[0];
	}

}
