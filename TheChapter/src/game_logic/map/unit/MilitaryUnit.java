package game_logic.map.unit;

import java.util.LinkedList;

import org.joml.Vector2f;
import org.joml.Vector2i;

import game_logic.map.unit.actions.combat.Defending;
import game_logic.map.unit.actions.combat.WaitingForCombat;
import game_logic.tile_object.Combatable;
import game_logic.tile_object.Damageable;
import game_logic.tile_object.Teamable;
import game_logic.tile_object.TileObject;

public abstract class MilitaryUnit extends Unit implements Combatable {

	public MilitaryUnit() {
	}

	@Override
	public final boolean isEnemy(Combatable obj) {
		return ((obj instanceof Teamable) ? (this.getMasterTeam() != ((Teamable) obj).getMasterTeam()
				&& (obj instanceof Damageable ? ((Damageable) obj).getCurrentHealth() > 0 : true)) : false);
	}

	@Override
	public final <T extends TileObject & Combatable> void defend(T opponent) {
		registerActionToAllSubjects(Defending.class, new Object[] { opponent.getAttackTime() });
	}

	@Override
	public final <T extends TileObject & Combatable> void waitBeforeCombat(T opponent, Marker mark) {
		registerActionToAllSubjects(WaitingForCombat.class, new Object[] { (Object) mark, opponent });
	}

	public final <T extends TileObject & Combatable> Vector2f rotateToOpponent(T op) {
		Vector2i this_pos = homeMap.getCoordinates(homeTile);
		Vector2i op_pos = op.getHomeMap().getCoordinates(op.getHomeTile());

		Vector2i dir = new Vector2i();

		op_pos.add(-this_pos.x, -this_pos.y, dir);

		return new Vector2f(dir.x, dir.y).normalize();
	}

	@Override
	public final <T extends TileObject & Combatable> Vector2f[] getTargetsScalarCoordinates(T opp) {
		LinkedList<Vector2f> res_list = new LinkedList<Vector2f>();

		for (int i = 0; i < subjects.length; i++) {
			Subject sub = subjects[i];
			if (!sub.isKilled()) {
				res_list.add(sub.getScalar_position());
			}
		}

		Vector2f[] result = new Vector2f[res_list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = res_list.get(result.length - i - 1);
		}
		return result;
	}
	
	public abstract int getAttackParticlesGeneratorIndex();

}
