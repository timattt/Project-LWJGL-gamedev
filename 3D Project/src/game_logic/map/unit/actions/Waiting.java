package game_logic.map.unit.actions;

public class Waiting extends UnitAction {

	public Waiting() {
		super("Wait");
	}

	@Override
	public void init(Object... arg) {
		total_action_time = (long) arg[0];
	}

}
