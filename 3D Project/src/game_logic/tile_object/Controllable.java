package game_logic.tile_object;

import game_logic.gui.controllers.TileObjectController;
import game_logic.map.Team;

public interface Controllable {
	public TileObjectController getGui();
	public boolean canControl(Team player);
}
