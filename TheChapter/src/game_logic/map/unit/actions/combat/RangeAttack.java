package game_logic.map.unit.actions.combat;

import java.util.LinkedList;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import game_logic.map.Tile;
import game_logic.map.decoration.Decoration;
import game_logic.map.unit.MilitaryUnit;
import game_logic.map.unit.RangeMilitaryUnit;
import game_logic.map.unit.actions.UnitAction;
import game_logic.storage.SoundConfigs;
import game_logic.tile_object.Combatable;
import game_logic.tile_object.TileObject;
import soundSupport.SoundManager;

public class RangeAttack extends UnitAction {

	// Target
	private Vector2f[] target;

	// Opponent
	private Combatable opponent;

	// Shell
	private Decoration[] shell;
	private long shell_flight_time;
	private Vector3f shell_position;

	// Points
	private Vector3f[] shell_start;
	private Vector3f[] shell_end;

	// Sound times
	private LinkedList<Long> shoot_times = new LinkedList<Long>();
	private LinkedList<Long> explosion_times = new LinkedList<Long>();

	// Decorations rotations
	private Vector3f[][] dirs;

	public RangeAttack() {
		super("RangeAttack");
	}

	@Override
	public void updateGraphics() {
		long startTime = subjects[0].getUnit().getActionEndTime(this) - total_action_time;

		for (int sub_i = 0; sub_i < subjects.length; sub_i++) {
			/*
			 * Finding position
			 */
			// Finding bullet scalar
			float scalar = (float) ((float) ((System.currentTimeMillis() - startTime) % shell_flight_time)
					/ (float) shell_flight_time);

			// Finding direction
			Vector3f dir = new Vector3f();
			shell_end[sub_i].add(-shell_start[sub_i].x, -shell_start[sub_i].y, -shell_start[sub_i].z, dir);
			dir.mul(scalar);

			// Finding final position
			Vector3f pos = new Vector3f(dir);
			pos.add(shell_start[sub_i]);

			/*
			 * Transforming decoration
			 */
			shell[sub_i].setPosition(pos);

			if (((RangeMilitaryUnit) subjects[0].getUnit()).getShootSound() != null && !shoot_times.isEmpty()
					&& shoot_times.getFirst() < System.currentTimeMillis()) {
				SoundManager.instance.addSoundToPlay(this.shell_start[sub_i], 1f, SoundConfigs.CONFIG1,
						(long) (System.currentTimeMillis() + Math.random() * 500l), 500l,
						((RangeMilitaryUnit) subjects[0].getUnit()).getShootSound());

				Vector3f pos1 = new Vector3f();
				pos1.set(this.shell_start[sub_i]);
				pos1.add((float) (2f * Math.random() - 1f), (float) (2f * Math.random() - 1f),
						(float) (2f * Math.random() - 1f));
				getMap().addParticle(((MilitaryUnit) this.subjects[0].getUnit()).getAttackParticlesGeneratorIndex(),
						pos1, System.currentTimeMillis(), (long) (5000.0 * Math.random()), (float) (2.0),
						(float) (1.0));

			}
			if (((RangeMilitaryUnit) subjects[0].getUnit()).getTargetHitSound() != null
					&& !this.explosion_times.isEmpty() && explosion_times.getFirst() < System.currentTimeMillis()) {
				SoundManager.instance.addSoundToPlay(this.shell_end[sub_i], 1f, SoundConfigs.CONFIG1,
						(long) (System.currentTimeMillis() + 500l * Math.random()), 1000l,
						((RangeMilitaryUnit) subjects[0].getUnit()).getTargetHitSound());
				Vector3f pos1 = new Vector3f();
				for (int i = 0; i < 1; i++) {
					pos1.set(this.shell_end[sub_i]);
					pos1.y = getMap().projectY(pos) + opponent.getSubjectFromPlane();
					pos1.add((float) (2f * Math.random() - 1f), 0, (float) (2f * Math.random() - 1f));
					getMap().addParticle(
							((RangeMilitaryUnit) this.subjects[0].getUnit()).getBulletFrapperParticleGeneratorIndex(),
							pos1, System.currentTimeMillis(), (long) (1000.0 * (1.0 + Math.random() / 10.0)),
							(float) (2.0), (float) (1.0));

				}
			}
		}

		if (!this.explosion_times.isEmpty() && this.explosion_times.getFirst() < System.currentTimeMillis()) {
			explosion_times.removeFirst();
		}

		if (!shoot_times.isEmpty() && this.shoot_times.getFirst() < System.currentTimeMillis()) {
			shoot_times.removeFirst();
		}
	}

	@Override
	public void init(Object... arg) {
		target = (Vector2f[]) arg[0];
		opponent = (Combatable) arg[1];
		shell = (Decoration[]) arg[2];
		shell_flight_time = (long) arg[3];
		shell_position = new Vector3f((Vector3f) arg[4]).mul(this.subjects[0].collectDecorations()[0].getScale());
		total_action_time = ((Combatable) subjects[0].getUnit()).getAttackTime();

		shell_end = new Vector3f[subjects.length];
		shell_start = new Vector3f[subjects.length];

		Vector2i start = getMap().getCoordinates(subjects[0].getUnit().getHomeTile());
		Vector2i end = getMap().getCoordinates(((TileObject) opponent).getHomeTile());

		Vector2i direction = new Vector2i();
		end.add(-start.x, -start.y, direction);

		float angle = new Vector2f(direction.x, direction.y).angle(TileObject.BASE_DIRECTION);

		for (int sub_i = 0; sub_i < subjects.length; sub_i++) {
			// Finding start
			Tile start_tile = subjects[0].getUnit().getHomeTile();
			Vector3f vec = new Vector3f(start_tile.center);
			Vector3f shell_pos = new Vector3f(shell_position);
			shell_pos.add(Decoration.calculateScalarPosition(subjects[sub_i].getScalar_position()));
			Quaternionf rot = getMap().createRotation(shell_pos);
			rot.rotateY(angle);
			shell_pos.rotate(rot);
			vec.add(shell_pos);
			shell_start[sub_i] = new Vector3f(vec);

			// Finding end
			Tile end_tile = ((TileObject) opponent).getHomeTile();
			vec = new Vector3f(end_tile.center);
			rot = getMap().createRotation(vec);
			shell_pos = new Vector3f(0, opponent.getSubjectFromPlane(), 0);
			shell_pos.add(Decoration.calculateScalarPosition(target[sub_i]));
			rot.rotateY((float) (Math.PI + angle));
			shell_pos.rotate(rot);
			vec.add(shell_pos);
			shell_end[sub_i] = new Vector3f(vec);

		}

		dirs = new Vector3f[subjects.length][2];

		Vector3f attack_dir = new Vector3f(subjects[0].getUnit().getHomeTile().center);
		attack_dir.negate();
		attack_dir.add(((TileObject) opponent).getHomeTile().center);

		attack_dir.y = 0f;

		Vector3f sub_dir;

		for (int sub_i = 0; sub_i < subjects.length; sub_i++) {
			sub_dir = new Vector3f(shell_start[sub_i]);
			sub_dir.negate();
			sub_dir.add(shell_end[sub_i]);
			sub_dir.y = 0f;
			dirs[sub_i][0] = attack_dir;
			dirs[sub_i][1] = sub_dir;
		}

	}

	@Override
	public void end() {
		for (Decoration shell : shell) {
			subjects[0].getUnit().getHomeMap().removeDecoration(shell);
		}
		return;
	}

	@Override
	public void start() {
		long curr = System.currentTimeMillis();
		for (int i = 0; i < (total_action_time / shell_flight_time); i++) {
			explosion_times.add(
					curr + (i + 1) * shell_flight_time + (i + 1 == (total_action_time / shell_flight_time) ? -50 : 0));
			shoot_times.add(curr + i * shell_flight_time);
		}

		for (Decoration shell : shell) {
			subjects[0].getUnit().getHomeMap().addDecoration(shell);
		}

		for (int sub_i = 0; sub_i < subjects.length; sub_i++) {
			for (Decoration dec : subjects[sub_i].collectDecorations(RangeAttack.class)) {
				dec.getRotationQuaternion().rotateTo(dirs[sub_i][0], dirs[sub_i][1]);
				dec.updateModelMatrix();
			}
		}
	}

}
