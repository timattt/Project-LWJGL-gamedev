package game_logic.map.unit.actions;

import game_logic.map.Map;
import game_logic.map.Tile;
import game_logic.map.unit.Subject;
import game_logic.map.unit.actions.combat.Defending;
import game_logic.map.unit.actions.combat.MeleeAttack;
import game_logic.map.unit.actions.combat.RangeAttack;
import game_logic.map.unit.actions.combat.RunToOpponent;
import game_logic.map.unit.actions.combat.WaitingForCombat;

public abstract class UnitAction {

	// Actions
	public static final Class<? extends UnitAction> STAND = Standing.class;
	public static final Class<? extends UnitAction> MOVE = Moving.class;
	public static final Class<? extends UnitAction> DAMAGE = Damaging.class;
	public static final Class<? extends UnitAction> WAITING_FOR_COMBAT = WaitingForCombat.class;
	public static final Class<? extends UnitAction> RANGE_ATTACK = RangeAttack.class;
	public static final Class<? extends UnitAction> DEFEND = Defending.class;
	public static final Class<? extends UnitAction> MELEE_ATTACK = MeleeAttack.class;
	public static final Class<? extends UnitAction> RUN_ATTACK = RunToOpponent.class;
	public static final Class<? extends UnitAction> WAITING = Waiting.class;
	public static final Class<? extends UnitAction> HEALING = Healing.class;
	
	// Name
	public final String name;

	// Time
	protected long total_action_time = -1;

	// Unit
	protected Subject[] subjects;

	// Tile
	protected Tile action_tile;
	
	public UnitAction(String name) {
		this.name = name;
	}

	public final Map getMap() {
		return subjects[0].getUnit().getHomeMap();
	}

	public void updateGraphics() {
	}

	public void init(Subject[] sub, Tile tile) {
		this.subjects = sub;
		this.action_tile = tile;
	}

	public void end() {
	}

	public void start() {
	}

	public void turn_started() {
	}

	public abstract void init(Object... arg);

	public void removeActionFromAllSubjects() {
		for (Subject sub : subjects) {
			sub.removeAction(this);
		}
	}

	public final Subject[] getSubjects() {
		return subjects;
	}

	public long getTotal_action_time() {
		return total_action_time;
	}

	public final Tile getAction_tile() {
		return action_tile;
	}

	public boolean canStart() {
		return true;
	}
	
	public boolean abandonPreparingToTile() {
		return false;
	}
}
