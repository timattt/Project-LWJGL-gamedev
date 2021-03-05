package game_logic.map.unit.actions;

import game_logic.map.unit.Subject;
import game_logic.map.unit.Unit;

public class Damaging extends UnitAction {

	private boolean kill;
	
	public Damaging() {
		super("Damaging");
	}

	@Override
	public void end() {
		for (Subject sub : subjects) {
			sub.setVisible(false);
		}
		if (kill) {
			Unit unit = subjects[0].getUnit();
			unit.getMasterTeam().removeTeamable(unit);
			unit.getHomeMap().removeTileObject(unit);
		}
	}

	@Override
	public void init(Object... arg) {
		kill = (boolean) arg[0];
		total_action_time = (long) arg[1];
	}

}
