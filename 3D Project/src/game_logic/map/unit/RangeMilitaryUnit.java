package game_logic.map.unit;

import java.util.HashMap;
import java.util.LinkedList;

import org.joml.Vector2f;
import org.joml.Vector3f;

import game_logic.map.decoration.Decoration;
import game_logic.map.unit.actions.combat.RangeAttack;
import game_logic.tile_object.Combatable;
import game_logic.tile_object.RangeCombatable;
import game_logic.tile_object.TileObject;
import graphicsSupport.mesh.Mesh;
import soundSupport.SoundBuffer;

public abstract class RangeMilitaryUnit extends MilitaryUnit implements RangeCombatable {

	// Shells
	protected HashMap<Subject, Decoration> shells = new HashMap<Subject, Decoration>();

	public RangeMilitaryUnit() {
		Mesh[] raw = loadShells();
		for (Subject sub : subjects) {
			shells.put(sub, new Decoration(raw, this, this.getShellScale()));
		}
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
		Decoration[] shells = new Decoration[subs_list.size()];

		for (i = 0; i < subs_list.size(); i++) {
			subs_arr[i] = subs_list.get(i);
			shells[i] = this.shells.get(subs_list.get(i));
		}

		if (targets.length != 0) {
			registerActionToSubjects(subs_arr, RangeAttack.class,
					new Object[] { targets, opponent, shells, getShellFlightTime(), getShellStartLocation() });
		}
	}

	public abstract Mesh[] loadShells();

	public abstract long getShellFlightTime();

	public abstract Vector3f getShellStartLocation();

	public abstract int getBulletFrapperParticleGeneratorIndex();
	
	public abstract SoundBuffer getShootSound();
	
	public abstract SoundBuffer getTargetHitSound();
	
	public float getShellScale() {
		return 1;
	}
	
	@Override
	public final void doCombat(Combatable opponent) {
		RangeCombatable.super.doCombat(opponent);
	}
}
