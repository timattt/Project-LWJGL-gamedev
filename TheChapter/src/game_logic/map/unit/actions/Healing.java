package game_logic.map.unit.actions;

public class Healing extends UnitAction {
	
	private float val;

	public Healing() {
		super("Healing");
	}

	@Override
	public void init(Object... arg) {
		val = (float) arg[0];
	}

	@Override
	public void end() {
		subjects[0].getUnit().setCurrent_health(Math.min(1, subjects[0].getUnit().getCurrentHealth() + val));
		int size = (int) (subjects[0].getUnit().getCurrentHealth() * subjects[0].getUnit().getSubjectsQuantity());

		for (int i = 0; i < size; i++) {
			if (i == subjects.length) {
				break;
			}
			subjects[i].setKilled(false);
			subjects[i].setVisible(true);
		}
	}
	
	

}
