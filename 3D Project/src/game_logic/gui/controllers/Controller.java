package game_logic.gui.controllers;

import java.util.LinkedList;

import org.joml.Vector2f;
import org.joml.Vector2i;

import controlSupport.MouseHandler;
import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInstance;
import game_logic.gui.controllers.selectors.TileSelector;
import game_logic.gui.events.Delegate;
import game_logic.gui.events.Events;
import game_logic.gui.events.EventsListener;
import game_logic.map.Map;
import game_logic.map.Tile;
import game_logic.map.Tile.TileVisibility;
import game_logic.map.player.Human;
import game_logic.tile_object.Controllable;
import graphicsSupport.gui.VgGui;
import graphicsSupport.gui.VgGuiHandler;

@MonoDemeanor
public class Controller implements VgGui, EventsListener {

	@MonoDemeanorInstance
	public static final Controller instance = new Controller();
	
	// Event system
	private Delegate delegate = new Delegate() {
		@Override
		public void newEvent(int index) {
			if (index == Events.EVENT_NEW_HUMAN_PLAYER_TURN) {
				turn = true;
				for (Controllable obj : objects_to_control) {
					obj.getGui().newTeamTurn();
				}
			}
			if (index == Events.EVENT_HUMAN_PLAYER_TURN_ENDED) {
				turn = false;
			}
		}

		@Override
		public void premise(Object premise, int index) {
			if (index == Events.PREMISE_NEW_MAP) {
				Controller.this.map = (Map) premise;
			}
		}
	};

	// Selector
	private Selector selector_decoration;
	private TileSelector tile_selector = new TileSelector() {
		@Override
		public void rightMouseButtonClickedTile(Tile tile) {

		}

		@Override
		public void leftMouseButtonClickedTile(Tile tile) {
			restore();

			Vector2i tile_coords = map.getCoordinates(tile);

			if (Human.instance.getTeam()
					.getVisibilityMap()[tile_coords.x][tile_coords.y] != TileVisibility.VISIBLE) {
				return;
			}

			// Setting selector decoration
			putSelector(tile_coords);

			LinkedList<Controllable> objs = tile.getAll(Controllable.class);

			for (int i = 0; i < objs.size(); i++) {
				if (((Controllable) objs.get(i)).canControl(Human.instance.getTeam())) {
					objects_to_control.add((Controllable) objs.get(i));
				}
			}

			for (Controllable contr : objects_to_control) {
				TileObjectController hud = contr.getGui();
				hud.set(contr, Controller.this);
				VgGuiHandler.instance.addHUD(hud);
			}

		}
	};

	// Turns
	private boolean turn;

	// Controller
	private LinkedList<Controllable> objects_to_control = new LinkedList<Controllable>();

	// Panel
	public static final float height_scalar = 0.5f;
	public static final float width_scalar = 0.17f;

	// Text
	public static final float x_scalar = 0.15f;

	// Instances
	private volatile Map map;

	public Controller() {
		selector_decoration = new Selector();
	}

	private void restore() {
		putSelector(null);
		for (Controllable contr : objects_to_control) {
			VgGuiHandler.instance.removeHUD(contr.getGui());
		}

		objects_to_control.clear();
	}

	public void putSelector(Vector2i dest) {
		if (map != null && selector_decoration.getHomeTile() != null) {
			map.removeTileObject(selector_decoration);
		}
		if (dest != null) {
			map.registerTileObject(selector_decoration, dest.x, dest.y);
		}
	}

	@Override
	public void update() {
		MouseHandler hand = MouseHandler.instance;
		if (!turn || map == null) {
			return;
		}
		// Checking mouse position to be outside of all components
		for (VgGui hud : VgGuiHandler.instance.getHuds()) {
			if (hud != this && hud.contains(hand.getMousePosition())) {
				return;
			}
		}

		for (int i = 0; i < objects_to_control.size(); i++) {
			if (!objects_to_control.get(i).canControl(Human.instance.getTeam())) {
				VgGuiHandler.instance.removeHUD(objects_to_control.get(i).getGui());
				objects_to_control.remove(i);
			}
		}

		// Selecting
		tile_selector.selectTile(map);
	}

	@Override
	public Delegate getDelegate() {
		return delegate;
	}

	@Override
	public boolean contains(Vector2f vec) {
		return false;
	}

	@Override
	public void render() {
	}

	@Override
	public void removed() {
		restore();
	}

}
