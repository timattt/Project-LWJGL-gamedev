package game_logic.map.unit.actions.combat;

import org.joml.Vector2f;

import game_logic.map.unit.MilitaryUnit;
import game_logic.map.unit.actions.UnitAction;
import game_logic.tile_object.Combatable;
import game_logic.tile_object.Combatable.Marker;
import game_logic.tile_object.TileObject;

public class WaitingForCombat extends UnitAction {

	// Opponent
	private Combatable opponent = null;

	// Direction
	private Vector2f direction;

	// Time
	private Marker marker;
	private int times = 0;
	private boolean ready = false;

	public WaitingForCombat() {
		super("Waiting for combat");
	}

	@Override
	public void updateGraphics() {
		if (marker.isReady((Combatable) subjects[0].getUnit())) {
			subjects[0].getUnit().setActionEndTime(this, 0);
		}
	}

	@Override
	public boolean canStart() {
		if (!ready) {
			marker.ready();
			ready = true;
		}
		times++;
		return times > 2;
	}

	@Override
	public void init(Object... arg) {
		marker = (Marker) arg[0];
		opponent = (Combatable) arg[1];
		direction = ((MilitaryUnit) subjects[0].getUnit()).rotateToOpponent((TileObject & Combatable) opponent);
	}

	@Override
	public boolean abandonPreparingToTile() {
		subjects[0].getUnit().setDirection(direction);
		return false;
	}

	@Override
	public long getTotal_action_time() {
		return ((marker.isReady((Combatable) subjects[0].getUnit())) ? -10l : System.currentTimeMillis());
	}

}
