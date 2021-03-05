package game_logic.map.unit.actions.combat;

import org.joml.Vector3f;

import game_logic.map.Map;
import game_logic.map.unit.actions.UnitAction;

public class MeleeAttack extends UnitAction {
	public MeleeAttack() {
		super("MeleeAttack");
	}

	@Override
	public void start() {
		for (int i = 0; i < subjects.length; i++) {
			Vector3f pos = new Vector3f(subjects[i].collectDecorations(MeleeAttack.class)[0].getPosition());
			pos.add(new Vector3f((float) Math.random() * 2f - 1f, (float) Math.random() * 2f - 1f,
					(float) Math.random() * 2f - 1f));
			subjects[0].getUnit().getHomeMap().addParticle(Map.PARTICLE_GEN_SPARKLES, pos, System.currentTimeMillis(),
					500l, 1f, 0.0001f);
		}
	}

	@Override
	public void init(Object... arg) {
		total_action_time = (long) arg[0];
	}

	@Override
	public boolean abandonPreparingToTile() {
		return true;
	}

}
