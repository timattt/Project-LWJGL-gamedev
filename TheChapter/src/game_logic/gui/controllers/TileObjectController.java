package game_logic.gui.controllers;

import game_logic.tile_object.Controllable;
import graphicsSupport.gui.VgGui;

public interface TileObjectController extends VgGui {
	
	public static final TileObjectController unit_controller = new UnitController();
	public static final TileObjectController settlement_controller = new SettlementController();
	
	public void newTeamTurn();
	public void set(Controllable cont, Controller controller);
}
