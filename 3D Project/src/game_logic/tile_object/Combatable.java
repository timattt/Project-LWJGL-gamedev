package game_logic.tile_object;

import org.joml.Vector2f;
import org.joml.Vector2i;

import game_logic.map.Map;

public interface Combatable {

	/**
	 * This method checks if two object can start combat right now. They can if:
	 * <p>
	 * 1. Both object are combatable. 2. Second object is in range of first. 3. If
	 * first object in movable then it checks if it has move points.
	 * 
	 * @param att
	 *            : First, attacking object.
	 * @param def
	 *            : Second, defending object.
	 * @return : True if they can start combat.
	 */
	public static boolean canAttack(TileObject att, TileObject def) {
		return (att instanceof Combatable) && (def instanceof Combatable)
				&& ((Combatable) att).inRange((TileObject & Combatable) def)
				&& ((att instanceof Movable) ? ((Movable) att).getCurrentMovePoints() > 0 : true);
	}

	/**
	 * When this method is invoked it tells object that it is attacking.
	 * 
	 * @param opponent
	 *            : Combat enemy.
	 */
	public <T extends TileObject & Combatable> void attack(T opponent);

	/**
	 * This method calculate attacking object damage and return it and all other
	 * parameters in the array. All array members are below.
	 * <p>
	 * [0] - strength; [1] - courage; [2] - depletion; [3] - strength ratio; [4] -
	 * damage;
	 * 
	 * @return : Array of parameters.
	 */
	public default <T extends TileObject & Combatable> float[] calculateAttackerDamage(T opponent) {
		float[] result = new float[5];
		result[0] = opponent.getStrength();
		result[1] = opponent.getCourage();
		if (opponent instanceof Movable) {
			result[2] = 0.5f * (float) ((float) ((Movable) opponent).getCurrentMovePoints()
					/ (float) ((Movable) opponent).getMovePoints()) + 0.5f;
		} else {
			result[2] = 1;
		}
		result[3] = (float) ((float) opponent.getStrength() / (float) this.getStrength());
		result[4] = result[1] * result[2] * result[3]
				* (opponent instanceof Damageable ? ((Damageable) opponent).getCurrentHealth() : 1);
		// Checking damage to be less then 1
		result[4] = Math.min(1, result[4]);
		return result;
	}

	/**
	 * This method calculate defending object damage and return it and all other
	 * parameters in the array. All array members are below.
	 * <p>
	 * [0] - strength; [1] - endurance; [2] - depletion; [3] - strength ratio; [4] -
	 * damage;
	 * 
	 * @return : Array of parameters.
	 */
	public default <T extends TileObject & Combatable> float[] calculateDefenderDamage(T opponent) {
		float[] result = new float[5];
		result[0] = getStrength();
		result[1] = getEndurance();
		if (this instanceof Movable) {
			result[2] = 0.5f * (float) ((float) ((Movable) this).getCurrentMovePoints()
					/ (float) ((Movable) this).getMovePoints()) + 0.5f;
		} else {
			result[2] = 1;
		}
		result[3] = (float) ((float) this.getStrength() / (float) opponent.getStrength());
		result[4] = result[1] * result[2] * result[3]
				* (this instanceof Damageable ? ((Damageable) this).getCurrentHealth() : 1);

		// Checking damage to be less then 1
		result[4] = Math.min(1, result[4]);
		return result;
	}

	/**
	 * When this method is invoked it tells object that it is defending.
	 * 
	 * @param opponent
	 *            : Combat enemy.
	 */
	public <T extends TileObject & Combatable> void defend(T opponent);

	/**
	 * This method does all combat calculations.
	 * 
	 * @param opponent
	 *            : Combat enemy.
	 */
	public void doCombat(Combatable opponent);

	/**
	 * This method tells how long will be objects attack.
	 * 
	 * @return : Attack time.
	 */
	public <T extends TileObject & Combatable> long getAttackTime();

	/**
	 * This method give courage parameter.
	 * 
	 * @return : Courage (смелость).
	 */
	public float getCourage();

	/**
	 * This method gives endurance parameter.
	 * 
	 * @return : Endurance (выносливость).
	 */
	public float getEndurance();

	/**
	 * This method gives object attack range if its enemy is in this range then
	 * object can attack it.
	 * 
	 * @return : Attack range.
	 */
	public int getRange();

	/**
	 * This method gives strength parameter.
	 * 
	 * @return : Strength (сила).
	 */
	public float getStrength();

	/**
	 * This method tells subject length to tile plane. 
	 * @return
	 */
	public default float getSubjectFromPlane() {
		return 1f;
	}
	
	/**
	 * This method return array of scalar positions on tile that represent targets
	 * for opponent object.
	 * 
	 * @return : Array of scalar positions.
	 */
	public <T extends TileObject & Combatable> Vector2f[] getTargetsScalarCoordinates(T opp);

	/**
	 * This method checks if enemy object is in range to attack.
	 * 
	 * @param enemy
	 *            : Combat enemy.
	 * @return : True if enemy is in range.
	 */
	public default <T extends TileObject & Combatable> boolean inRange(T enemy) {
		Map map = ((TileObject & Combatable) this).getHomeMap();
		Vector2i loc_att = map.getCoordinates(((TileObject & Combatable) this).getHomeTile());
		Vector2i loc_def = map.getCoordinates(enemy.getHomeTile());

		Vector2i dir = new Vector2i();

		loc_def.add(-loc_att.x, -loc_att.y, dir);
		return Math.abs(dir.x) <= getRange() && Math.abs(dir.y) <= getRange();
	}

	/**
	 * This method checks if the given object is enemy for this object.
	 * 
	 * @param obj
	 *            : Object to check.
	 * @return : True if enemy.
	 */
	public boolean isEnemy(Combatable obj);

	/**
	 * When this method is invoked it tells object that it must wait before doing
	 * anything else.
	 * 
	 * @param opponent
	 *            : Combat enemy.
	 * @param time
	 *            : Time to wait.
	 */
	public <T extends TileObject & Combatable> void waitBeforeCombat(T opponent, Marker marker);

	public class Marker {
		protected boolean[] marks = new boolean[2];
		protected int index = 0;

		public void ready() {
			marks[index] = true;
			index++;
		}

		public boolean isReady(Combatable comb) {
			return marks[0] && marks[1];
		}

		public int getLastIndex() {
			return index;
		}
	}

}
