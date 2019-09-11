package game_logic.map.unit;

import java.util.LinkedList;

import org.joml.Vector2f;

import game_logic.map.unit.actions.combat.MeleeAttack;
import game_logic.map.unit.actions.combat.RunToOpponent;
import game_logic.tile_object.Combatable;
import game_logic.tile_object.MeleeCombatable;
import game_logic.tile_object.TileObject;

public abstract class MeleeMilitaryUnit extends MilitaryUnit implements MeleeCombatable {

	public MeleeMilitaryUnit() {
	}

	@Override
	public final <T extends TileObject & Combatable> void attack(T opponent) {
		Vector2f[] targets = opponent.getTargetsScalarCoordinates(this);
		LinkedList<Subject> subs_list = new LinkedList<Subject>();

		// Registering action for all subjects
		int i = 0;
		for (Subject sub : subjects) {
			if (targets.length == i) {
				break;
			}
			if (sub.isKilled()) {
				continue;
			}
			subs_list.add(sub);
			i++;
		}

		Subject[] subs_arr = new Subject[subs_list.size()];

		for (i = 0; i < subs_list.size(); i++) {
			subs_arr[i] = subs_list.get(i);
		}

		if (targets.length != 0) {
			registerActionToSubjects(subs_arr, MeleeAttack.class, new Object[] { this.getAttackTime() });
		}
	}

	@Override
	public final <T extends TileObject & Combatable> void runToOpponent(T opponent, Marker mark) {
		Vector2f[] targets = opponent.getTargetsScalarCoordinates(this);
		
		LinkedList<Subject> subs_list = new LinkedList<Subject>();

		// Registering action for all subjects
		int i = 0;
		for (Subject sub : subjects) {
			if (targets.length == i) {
				break;
			}
			if (sub.isKilled()) {
				continue;
			}
			subs_list.add(sub);
			i++;
		}

		Subject[] subs_arr = new Subject[subs_list.size()];

		for (i = 0; i < subs_list.size(); i++) {
			subs_arr[i] = subs_list.get(i);
		}

		if (targets.length != 0) {
			registerActionToSubjects(subs_arr, RunToOpponent.class,
					new Object[] { opponent, targets, this.getDistanceFromEnemy(), this.getRunTime(), mark });
		}
	}

	public abstract float getDistanceFromEnemy();
	
	@Override
	public final <T extends TileObject & Combatable> boolean inRange(T enemy) {
		return MeleeCombatable.super.inRange(enemy);
	}

	@Override
	public final void doCombat(Combatable opponent) {
		MeleeCombatable.super.doCombat(opponent);
	}

}
