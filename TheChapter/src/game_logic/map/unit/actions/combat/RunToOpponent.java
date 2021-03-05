package game_logic.map.unit.actions.combat;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import game_logic.map.Tile;
import game_logic.map.decoration.Decoration;
import game_logic.map.unit.actions.UnitAction;
import game_logic.tile_object.Combatable;
import game_logic.tile_object.Combatable.Marker;
import game_logic.tile_object.TileObject;

public class RunToOpponent extends UnitAction {

	// Opponent
	private Combatable opponent;
	private float distance_from_opponent;
	private Vector2f[] target;

	// Points
	private Vector3f[] p0;
	private Vector3f[] p1;
	private Vector3f[] p2;
	
	private float angle;

	private float[] ratio;
	private float[] p1_p0;
	private float[] p2_p1;

	private Vector3f[] sparkles;

	private Marker marker;

	public RunToOpponent() {
		super("Run to opponent");
	}

	@Override
	public void updateGraphics() {
		long startTime = subjects[0].getUnit().getActionEndTime(this) - total_action_time;
		float scalar = (float) ((float) (System.currentTimeMillis() - startTime) / (float) total_action_time);
		
		for (int sub_i = 0; sub_i < subjects.length; sub_i++) {
			Quaternionf rot;
			Vector3f pos;
			Vector3f dir = new Vector3f();
			if (scalar <= (p1_p0[sub_i] / (p1_p0[sub_i] + p2_p1[sub_i]))) {
				p1[sub_i].add(-p0[sub_i].x, -p0[sub_i].y, -p0[sub_i].z, dir);
				dir.mul(scalar / (p1_p0[sub_i] / (p1_p0[sub_i] + p2_p1[sub_i])));
				pos = new Vector3f(p0[sub_i]);
				pos.add(dir);
				rot = getMap().createRotation(pos);
			

			} else {
				p2[sub_i].add(-p1[sub_i].x, -p1[sub_i].y, -p1[sub_i].z, dir);
				dir.mul((scalar - ratio[sub_i]) / (p2_p1[sub_i] / (p1_p0[sub_i] + p2_p1[sub_i])));
				pos = new Vector3f(p1[sub_i]);
				pos.add(dir);
				rot = getMap().createRotation(pos);
			}

			for (Decoration dec : subjects[sub_i].collectDecorations(this.getClass())) {
				dec.setPosition(pos);
				dec.rotate(rot.rotateY(angle));
			}

		}
	}

	@Override
	public void end() {
		for (int sub_i = 0; sub_i < subjects.length; sub_i++) {
			for (Decoration dec : subjects[sub_i].collectDecorations(MeleeAttack.class)) {
				dec.setPosition(p2[sub_i]);
				dec.rotate(getMap().createRotation(p2[sub_i]).rotateY(angle));
			}
		}
		marker.ready();
	}

	@Override
	public void init(Object... arg) {
		opponent = (Combatable) arg[0];
		target = (Vector2f[]) arg[1];
		distance_from_opponent = (Float) arg[2];
		total_action_time = (long) arg[3];
		marker = (Marker) arg[4];

		ratio = new float[subjects.length];
		p0 = new Vector3f[subjects.length];
		p1 = new Vector3f[subjects.length];
		p2 = new Vector3f[subjects.length];
		p2_p1 = new float[subjects.length];
		p1_p0 = new float[subjects.length];
		sparkles = new Vector3f[subjects.length];
		
		Vector3f p1_p0 = new Vector3f();
		Vector3f p2_p1 = new Vector3f();

		Vector2i start = getMap().getCoordinates(subjects[0].getUnit().getHomeTile());
		Vector2i end = getMap().getCoordinates(((TileObject) opponent).getHomeTile());

		Vector2i direction = new Vector2i();
		end.add(-start.x, -start.y, direction);

		angle = new Vector2f(direction.x, direction.y).angle(TileObject.BASE_DIRECTION);
		
		for (int sub_i = 0; sub_i < subjects.length; sub_i++) {
			
			p0[sub_i] = getPositionFromScalarLocation(subjects[0].getUnit().getHomeTile(), subjects[sub_i].getScalar_position(), subjects[0].getUnit(), angle);

			p2[sub_i] = getPositionFromScalarLocation(((TileObject) opponent).getHomeTile(), target[sub_i],
					((TileObject) opponent), (float) (Math.PI + angle));

			p1[sub_i] = new Vector3f();
			p0[sub_i].add(p2[sub_i], p1[sub_i]);
			p1[sub_i].mul(0.5f);
			p1[sub_i].y = getMap().projectY(p1[sub_i]);

			Vector3f dir = new Vector3f();

			p2[sub_i].add(-p1[sub_i].x, -p1[sub_i].y, -p1[sub_i].z, dir);
			Vector3f dir1 = new Vector3f(dir);
			dir.mul(1f - distance_from_opponent / dir.length());

			dir1.mul(0.9f);
			dir1.add(p1[sub_i]);

			sparkles[sub_i] = dir1;

			p1[sub_i].add(dir, p2[sub_i]);

			p1[sub_i].add(-p0[sub_i].x, -p0[sub_i].y, -p0[sub_i].z, p1_p0);
			p2[sub_i].add(-p1[sub_i].x, -p1[sub_i].y, -p1[sub_i].z, p2_p1);

			ratio[sub_i] = p1_p0.length() / (p2_p1.length() + p1_p0.length());

			this.p1_p0[sub_i] = p1_p0.length();
			this.p2_p1[sub_i] = p2_p1.length();
		}
	}
	
	private Vector3f getPositionFromScalarLocation(Tile tile, Vector2f scalar, TileObject obj, float angle) {
		Vector3f base_pos = new Vector3f(tile.center);

		Quaternionf y_rot = new Quaternionf();
		y_rot.fromAxisAngleRad(new Vector3f(0, 1, 0), angle);

		Vector3f scalar_pos = Decoration.calculateScalarPosition(scalar);
		scalar_pos.rotate(y_rot);

		base_pos.add(scalar_pos);

		base_pos.y = getMap().projectY(base_pos);

		return base_pos;
	}

	@Override
	public boolean abandonPreparingToTile() {
		return true;
	}

}
