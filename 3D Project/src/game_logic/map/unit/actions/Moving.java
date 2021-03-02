package game_logic.map.unit.actions;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import game_logic.map.Tile;
import game_logic.map.decoration.Decoration;
import game_logic.map.unit.Unit;
import game_logic.storage.SoundConfigs;
import game_logic.tile_object.TileObject;
import soundSupport.SoundManager;
import soundSupport.SoundSource;

public class Moving extends UnitAction {

	// Path lines
	private Vector3f[] path_lines;

	// Path
	private Tile[] path;

	// Direction
	private Vector2f direction;

	// Sound
	private SoundSource sound_source;

	public Moving() {
		super("Move");
	}

	@Override
	public void start() {
		if (subjects[0].getUnit().getMoveSound() != null) {
			sound_source = SoundManager.instance.addSoundToPlay(path_lines[0], 1f, SoundConfigs.CONFIG2,
					System.currentTimeMillis(), this.total_action_time, subjects[0].getUnit().getMoveSound());

		}
		if (sound_source != null) {
			sound_source.setPitch(1.3f);
		}
	}

	@Override
	public void updateGraphics() {

		long currTime = System.currentTimeMillis();
		long startTime = subjects[0].getUnit().getActionEndTime(this) - total_action_time;

		// Finding position on the line between two centers
		long pieceTime = total_action_time / (path_lines.length - 1);
		int intervalIndex = (int) ((currTime - startTime) / pieceTime);
		float scalar = (float) ((float) ((currTime - startTime) % pieceTime) / (float) pieceTime);

		for (int sub_i = 0; sub_i < subjects.length; sub_i++) {

			Vector3f base_pos = new Vector3f();

			// Position
			Vector3f dir = new Vector3f();
			path_lines[intervalIndex + 1].add(-path_lines[intervalIndex].x, -path_lines[intervalIndex].y,
					-path_lines[intervalIndex].z, dir);
			if (scalar != 0f) {
				dir.mul(scalar);
				path_lines[intervalIndex].add(dir, base_pos);
			} else {
				base_pos.set(path_lines[intervalIndex]);
			}

			// Making dir parallel to plane XZ
			dir.y = 0;
			dir.normalize();

			Vector3f scalar_pos;
			Vector3f final_pos;
			Quaternionf rot;
			Vector3f tile_pos = new Vector3f();

			// Y rotation
			Quaternionf y_rot = new Quaternionf();

			float angle = new Vector2f(dir.x, dir.z).angle(TileObject.BASE_DIRECTION);
			
			y_rot.fromAxisAngleRad(new Vector3f(0, 1, 0), angle);

			for (Decoration dec : subjects[sub_i].collectDecorations(this.getClass())) {
				// Finding rotated scalar position
				scalar_pos = dec.calculateScalarPosition();
				scalar_pos.rotate(y_rot);

				// Finding tile
				base_pos.add(scalar_pos, tile_pos);

				// Finding position
				final_pos = new Vector3f(base_pos);
				final_pos.add(scalar_pos);

				final_pos.y = getMap().projectY(final_pos);

				// Rotating and moving
				dec.setPosition(final_pos);

				// Rotating to tile plane
				// old way
				/*
				rot = getMap().createRotation(final_pos);
				rot.rotateY(angle);
				*/
				rot = new Quaternionf().rotateY(angle);
				rot.mul(getMap().createRotation(final_pos));
				dec.rotate(rot);
			}

			// Badge
			rot = getMap().createRotation(base_pos);

			Vector3f badge_height = subjects[0].getUnit().getUnitBadge().getLocation().zero().set(0, Unit.badge_height,
					0);
			badge_height.rotate(y_rot);

			badge_height.add(base_pos);

			if (sound_source != null) {
				sound_source.setPosition(base_pos);
			}
		}
	}

	@Override
	public void init(Object... arg) {
		this.path = (Tile[]) arg[0];
		long tile_move_time = (long) arg[1];

		// Direction
		Vector2i direction = new Vector2i();
		subjects[0].getUnit().getHomeMap().getCoordinates(path[path.length - 1]).add(
				-subjects[0].getUnit().getHomeMap().getCoordinates(path[path.length - 2]).x,
				-subjects[0].getUnit().getHomeMap().getCoordinates(path[path.length - 2]).y, direction);

		this.direction = new Vector2f(direction.x, direction.y).normalize();

		// Time
		total_action_time = (path.length - 1) * tile_move_time;

		// Path
		path_lines = new Vector3f[path.length * 2 - 1];

		Vector2i dir = new Vector2i();

		Vector2i loc_start;
		Vector2i loc_end;

		Vector3f p1 = null;
		Vector3f p2 = null;

		Vector3f pos = new Vector3f();

		for (int sub_i = 0; sub_i < subjects.length; sub_i++)
			for (int i = 0; i < path_lines.length; i = i + 2) {
				// Center
				path_lines[i] = path[i / 2].center;

				if (i / 2 >= path.length - 1) {
					break;
				}

				// Other
				loc_start = getMap().getCoordinates(path[i / 2]);
				loc_end = getMap().getCoordinates(path[i / 2 + 1]);
				loc_end.add(-loc_start.x, -loc_start.y, dir);

				if (dir.x > 0 & dir.y > 0) {
					p1 = path[i / 2].p3;
					p2 = path[i / 2].p3;
				}
				if (dir.x == 0 & dir.y > 0) {
					p1 = path[i / 2].p3;
					p2 = path[i / 2].p4;
				}
				if (dir.x < 0 & dir.y > 0) {
					p1 = path[i / 2].p4;
					p2 = path[i / 2].p4;
				}
				if (dir.x < 0 & dir.y == 0) {
					p1 = path[i / 2].p1;
					p2 = path[i / 2].p4;
				}
				if (dir.x < 0 & dir.y < 0) {
					p1 = path[i / 2].p1;
					p2 = path[i / 2].p1;
				}
				if (dir.x == 0 & dir.y < 0) {
					p1 = path[i / 2].p2;
					p2 = path[i / 2].p1;
				}
				if (dir.x > 0 & dir.y < 0) {
					p1 = path[i / 2].p2;
					p2 = path[i / 2].p2;
				}
				if (dir.x > 0 & dir.y == 0) {
					p1 = path[i / 2].p2;
					p2 = path[i / 2].p3;
				}

				pos = new Vector3f();
				p1.add(p2, pos);

				pos.mul(0.5f);
				path_lines[i + 1] = pos;
			}

	}

	@Override
	public void end() {
		subjects[0].getUnit().setDirection(direction);
	}

}
