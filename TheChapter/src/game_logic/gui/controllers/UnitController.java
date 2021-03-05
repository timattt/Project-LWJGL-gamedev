package game_logic.gui.controllers;

import java.util.LinkedList;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import engine.Window;
import game_logic.gui.controllers.selectors.CombatSelector;
import game_logic.gui.controllers.selectors.DestinationSelector;
import game_logic.gui.controllers.selectors.TileSelector;
import game_logic.map.Map;
import game_logic.map.Tile;
import game_logic.map.Tile.TileVisibility;
import game_logic.map.player.Human;
import game_logic.map.unit.RangeMilitaryUnit;
import game_logic.map.unit.Unit;
import game_logic.map.unit.actions.permanent.PermanentHealing;
import game_logic.storage.Textures;
import game_logic.tile_object.Combatable;
import game_logic.tile_object.Controllable;
import game_logic.tile_object.Damageable;
import game_logic.tile_object.Movable;
import game_logic.tile_object.RangeCombatable;
import game_logic.tile_object.TileObject;
import game_logic.tile_object.moving.RouteCreator.RouteCreationStatus;
import graphicsSupport.gui.VgGui;
import graphicsSupport.gui.VgGuiHandler;
import graphicsSupport.gui.components.VgButton;
import graphicsSupport.gui.components.VgPanel;

public class UnitController implements TileObjectController {

	// GUI
	private UnitPermanentActionsPanel permanentActions = new UnitPermanentActionsPanel();
	private UnitInformationPanel unitInfo = new UnitInformationPanel();
	private UnitCombatInformationPanel unitCombatInfo = new UnitCombatInformationPanel();

	// Unit
	private Unit unit;

	// Controller
	private Controller controller;

	// Keys
	private final int key_delete_route = GLFW.GLFW_KEY_SPACE;

	// Selecting tiles
	private DestinationSelector[] destination_selectors = new DestinationSelector[0];
	private CombatSelector combat_selector = new CombatSelector();
	private Tile selected_tile;
	private TileSelector tile_selector = new TileSelector() {

		@Override
		public void rightMouseButtonClickedTile(Tile tile) {
			Vector2i coords = unit.getHomeMap().getCoordinates(tile);
			if (Human.instance.getTeam().getVisibilityMap()[coords.x][coords.y] == TileVisibility.HIDDEN) {
				return;
			}
			unitCombatInfo.opponent = null;
			Combatable enemy = null;
			RouteCreationStatus status = new RouteCreationStatus();
			Map map = unit.getHomeMap();

			removeDestinationSelectors();

			// If the player clicked the tile twice
			if (selected_tile == tile) {
				boolean is_combatable = unit instanceof Combatable;

				// Checking if unit can do combat and not move
				if (is_combatable) {
					enemy = tile.getEnemy((Combatable) unit);
					if (enemy != null && Combatable.canAttack(unit, (TileObject) enemy)) {
						((Combatable) unit).doCombat(enemy);
						return;
					}
				}

				// If no then we just move our unit
				map.moveObject(unit, map.getCoordinates(tile), status);

				// If unit moved and stops before let and the let is enemy then
				// the unit will fight with it
				if (is_combatable && Combatable.canAttack(unit, (TileObject) enemy)) {
					((Combatable) unit).doCombat(enemy);
				}

				controller.putSelector(unit.getHomeMap().getCoordinates(unit.getHomeTile()));
				refresh();
				selected_tile = null;
			} else {
				// If the player clicked tile at the first time

				// Finding start and end points of the path
				Vector2i start = map.getCoordinates(unit.getHomeTile());
				Vector2i end = map.getCoordinates(tile);

				// If start equals to end then it may be error
				if (start.x == end.x && start.y == end.y) {
					return;
				}

				// Generating route
				Tile[] route = Movable.findBestRoute(unit, end, status);
				// If the route is blocked
				if (route.length < 1) {
					return;
				}

				// If unit is military then combating signs can be created
				if (unit instanceof Combatable) {
					// If unit is military then we can see if selected tile
					// contains enemy for it
					enemy = tile.getEnemy((Combatable) unit);

					if (enemy != null) {
						unit.getHomeMap().registerTileObject(combat_selector, coords.x, coords.y);
						unitCombatInfo.setUnit(enemy);
					}

				}

				// If unit is not military then it can only moves
				if (!Combatable.canAttack(unit, (TileObject) enemy)) {
					registerNewDestinationSelectors(route);
				}

				selected_tile = tile;
			}
		}

		@Override
		public void leftMouseButtonClickedTile(Tile tile) {

		}

	};

	private void removeDestinationSelectors() {
		for (DestinationSelector sel : destination_selectors) {
			unit.getHomeMap().removeTileObject(sel);
		}
		unit.getHomeMap().removeTileObject(combat_selector);
		destination_selectors = new DestinationSelector[0];
	}

	private void registerNewDestinationSelectors(Tile[] path) {
		destination_selectors = new DestinationSelector[path.length - 1];

		int i = 0;
		for (Tile tile : path) {
			if (tile == path[0]) {
				continue;
			}
			destination_selectors[i] = new DestinationSelector();
			Vector2i coords = unit.getHomeMap().getCoordinates(tile);
			unit.getHomeMap().registerTileObject(destination_selectors[i], coords.x, coords.y);
			i++;
		}

	}

	private void registerNewAttackBorder() {
		if (!(unit instanceof RangeMilitaryUnit)) {
			return;
		}
		Vector2i unitPos = unit.getHomeMap().getCoordinates(unit.getHomeTile());
		int rad = ((RangeMilitaryUnit) unit).getRange();

		Tile[][] tiles = unit.getHomeMap().getTiles();

		Tile[] add_t = new Tile[8 * (rad) + 4];
		Vector2i[] add_c = new Vector2i[8 * (rad)];
		LinkedList<Tile> enemies = new LinkedList<Tile>();

		int index = 0;
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[0].length; y++) {
				Vector2i pos = new Vector2i(unitPos);
				pos.negate().add(unit.getHomeMap().getCoordinates(tiles[x][y]));

				if (Math.max(Math.abs(pos.x), Math.abs(pos.y)) == rad) {
					add_t[index] = tiles[x][y];
					add_c[index] = pos;
					index++;
				}

				if (Math.max(Math.abs(pos.x), Math.abs(pos.y)) <= rad) {
					if (tiles[x][y].getEnemy((Combatable) unit) != null) {
						enemies.add(tiles[x][y]);
					}
				}
			}
		}

		for (int i = 0; i < enemies.size(); i++) {
			unit.getHomeMap().setTextureToTileMapMesh(Textures.ATTACK_SIGN, enemies.get(i), 0);
		}

		for (int i = 0; i < 8 * (rad); i++) {
			if (add_c[i] == null || add_t[i] == null) {
				continue;
			}
			if (add_c[i].x == rad) {
				unit.getHomeMap().setTextureToTileMapMesh(Textures.ATTACK_BORDER, add_t[i], 180);
			}
			if (add_c[i].x == -rad) {
				unit.getHomeMap().setTextureToTileMapMesh(Textures.ATTACK_BORDER, add_t[i], 0);
			}
			if (add_c[i].y == rad) {
				unit.getHomeMap().setTextureToTileMapMesh(Textures.ATTACK_BORDER, add_t[i], 270);
			}
			if (add_c[i].y == -rad) {
				unit.getHomeMap().setTextureToTileMapMesh(Textures.ATTACK_BORDER, add_t[i], 90);
			}
		}

	}

	private void removeAttackBorder() {
		if (!(unit instanceof RangeMilitaryUnit)) {
			return;
		}

		unit.getHomeMap().getTileMapMesh().removeTextureFromAllTiles(Textures.ATTACK_BORDER);
		unit.getHomeMap().getTileMapMesh().removeTextureFromAllTiles(Textures.ATTACK_SIGN);
	}

	public UnitController() {
	}

	@Override
	public void update() throws InterruptInput {
		this.permanentActions.update();
		tile_selector.selectTile(unit.getHomeMap());
		if (GLFW.glfwGetKey(Window.instance.getWindowID(), key_delete_route) == GLFW.GLFW_PRESS) {
			removeDestinationSelectors();
			selected_tile = null;
			unit.getMasterTeam().removeMovable(unit);
			unitCombatInfo.opponent = null;
			VgGui.interruptInput();
		}
	}

	@Override
	public void set(Controllable cont, Controller controller) {
		this.controller = controller;
		unit = (Unit) cont;
		selected_tile = null;

		refresh();
	}

	private void refresh() {
		removeDestinationSelectors();
		removeAttackBorder();
		if (unit.getMasterTeam().containsMovable(unit)) {
			Vector2i start = unit.getHomeMap().getCoordinates(unit.getHomeTile());
			Vector2i end = unit.getMasterTeam().getMovableDestination(unit);

			if (start.x == end.x && start.y == end.y) {
				return;
			}

			Tile[] route = Movable.findBestRoute(unit, end, new RouteCreationStatus());
			registerNewDestinationSelectors(route);

		}
		registerNewAttackBorder();
	}

	@Override
	public boolean contains(Vector2f vec) {
		return unitInfo.isInComponent(vec) || unitCombatInfo.isInComponent(vec) || this.permanentActions.contains(vec);
	}

	@Override
	public void render() {
		unitInfo.render();
		unitCombatInfo.render();
		permanentActions.render();
	}

	private class UnitPermanentActionsPanel implements VgGui {

		private UnitActionButton[] actionButtons = new UnitActionButton[] { new UnitActionButton(0, "Heal", 0.019f) {
			@Override
			protected void clicked_left() {
				unit.registerPermanentAction(PermanentHealing.class, new Object[] { 0.1f });
			}

			@Override
			protected boolean hightlight() {
				return unit.getCurrentPermanentAction() != null
						&& unit.getCurrentPermanentAction().getClass() == PermanentHealing.class;
			}
		}, new UnitActionButton(1, "Kill", 0.019f) {

			@Override
			protected boolean hightlight() {
				return false;
			}

			@Override
			protected void clicked_left() {
				unit.damage(unit.getCurrentHealth());
			}

		} };

		@Override
		public void update() {
			for (int i = 0; i < actionButtons.length; i++) {
				actionButtons[i].update();
			}
		}

		@Override
		public boolean contains(Vector2f vec) {
			for (int i = 0; i < actionButtons.length; i++) {
				if (actionButtons[i].isInComponent(vec)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void render() {
			for (int i = 0; i < actionButtons.length; i++) {
				actionButtons[i].render();
			}
		}

	}

	private abstract class UnitActionButton extends VgButton {

		public UnitActionButton(int i, String text, float ind) {
			super(new Vector2f(0f, 0.15f + i * 0.07f), new Vector2f(0.1f, 0.2f + i * 0.07f), ind, text);
			setRad(0.005f);
			setTextHeight(0.03f);
			setIndentationY(0.015f);
		}

		protected abstract boolean hightlight();

		@Override
		public void render() {
			if (this.hightlight()) {
				boolean a = pressed_left;
				pressed_left = true;
				super.render();
				pressed_left = a;
			} else {
				super.render();
			}
		}

	}

	private class UnitInformationPanel extends VgPanel {

		public UnitInformationPanel() {
			super(new Vector2f(0f, 1f), new Vector2f(0.27f, 0.65f));
		}

		@Override
		public void render() {
			super.render();
			if (unit == null) {
				return;
			}
			VgGuiHandler vg = VgGuiHandler.instance;
			float y = 0.68f;

			// Name
			renderText(vg, "Name: " + unit.getName(), new Vector2f(0.02f, y), 0.03f);

			// Move points
			y += 0.035f;
			renderText(vg, "Move points: " + unit.getCurrentMovePoints() + "/" + unit.getMovePoints(),
					new Vector2f(0.02f, y), 0.03f);

			float move = (float) ((float) unit.getCurrentMovePoints() / (float) unit.getMovePoints()) * 0.11f;

			drawRect(0.15f, y, 0.11f, 0.03f, 0, 0, 0, 200, vg);
			drawRect(0.151f, y + 0.001f, move - 0.002f, 0.028f, 0, 0, 255, 200, vg);

			// Health
			y += 0.035f;
			renderText(vg, "Health: " + (unit.getCurrentHealth() + "0000").substring(0, 4), new Vector2f(0.02f, y),
					0.03f);
			float size = (float) (0.11f * (float) unit.getCurrentHealth());

			drawRect(0.15f, y + 0.001f, 0.11f, 0.03f, 0, 0, 0, 200, vg);
			drawRect(0.151f, y + 0.002f, size - 0.002f, 0.028f, 0, 255, 0, 200, vg);

			// Combat
			if (!(unit instanceof Combatable)) {
				return;
			}
			Combatable comb = (Combatable) unit;

			// Strength
			y += 0.07f;
			renderText(vg, "Strength: " + comb.getStrength(), new Vector2f(0.02f, y), 0.03f);

			// Courage
			y += 0.035f;
			renderText(vg, "Courage: " + comb.getCourage(), new Vector2f(0.02f, y), 0.03f);

			// Endurance
			y += 0.035f;
			renderText(vg, "Endurance: " + comb.getEndurance(), new Vector2f(0.02f, y), 0.03f);

			// Depletion
			y += 0.035f;
			renderText(vg, "Depletion: " + cut_float(
					(0.5f * (float) ((float) unit.getCurrentMovePoints() / (float) unit.getMovePoints()) + 0.5f)),
					new Vector2f(0.02f, y), 0.03f);

			// Range
			if (unit instanceof RangeCombatable) {
				y += 0.035f;
				renderText(vg, "Range: " + comb.getRange(), new Vector2f(0.02f, y), 0.03f);
			}

		}

	}

	private class UnitCombatInformationPanel extends VgPanel {

		private Combatable opponent;

		// Position
		private final float start_y = 0.3f;

		public UnitCombatInformationPanel() {
			super(new Vector2f(0f, 0.647f), new Vector2f(0.37f, 0.29f));
		}

		public final void setUnit(Combatable opp) {
			this.opponent = opp;
		}

		@Override
		public void render() {
			if (unit == null || opponent == null) {
				return;
			}
			super.render();
			VgGuiHandler vg = VgGuiHandler.instance;

			float[] info_a = ((Combatable) unit).calculateAttackerDamage((TileObject & Combatable) opponent);
			float[] info_d = ((Combatable) unit).calculateDefenderDamage((TileObject & Combatable) opponent);

			renderText(vg, "Attacker:", new Vector2f(0.03f, start_y), 0.04f);
			renderText(vg, "Strength:: " + cut_float(info_d[0]), new Vector2f(0.03f, start_y + 0.05f), 0.03f);
			renderText(vg, "Endurance: " + cut_float(info_d[1]), new Vector2f(0.03f, start_y + 0.08f), 0.03f);
			renderText(vg, "Depletion: " + cut_float(info_d[2]), new Vector2f(0.03f, start_y + 0.11f), 0.03f);
			renderText(vg, "Strength ratio: " + cut_float(info_d[3]), new Vector2f(0.03f, start_y + 0.14f), 0.03f);
			renderText(vg, "Damage: " + cut_float(info_a[4]), new Vector2f(0.03f, start_y + 0.17f), 0.03f);
			renderText(vg, "Health: " + cut_float(unit.getCurrentHealth()), new Vector2f(0.03f, start_y + 0.2f), 0.03f);

			float size = (float) (0.11f * (float) unit.getCurrentHealth());

			drawRect(0.02f, start_y + 0.25f, 0.11f, 0.03f, 0, 0, 0, 200, vg);

			drawRect(0.021f, start_y + 0.251f, size - 0.002f, 0.028f, 200, 0, 0, 255, vg);
			drawRect(0.021f, start_y + 0.251f, Math.max(0, size - info_a[4] * 0.11f - 0.002f), 0.028f, 0, 200, 0, 255,
					vg);

			renderText(vg, "Defender:", new Vector2f(0.2f, start_y), 0.04f);
			renderText(vg, "Strength:: " + cut_float(info_a[0]), new Vector2f(0.2f, start_y + 0.05f), 0.03f);
			renderText(vg, "Courage: " + cut_float(info_a[1]), new Vector2f(0.2f, start_y + 0.08f), 0.03f);
			renderText(vg, "Depletion: " + cut_float(info_a[2]), new Vector2f(0.2f, start_y + 0.11f), 0.03f);
			renderText(vg, "Strength ratio: " + cut_float(info_a[3]), new Vector2f(0.2f, start_y + 0.14f), 0.03f);
			renderText(vg, "Damage: " + cut_float(info_d[4]), new Vector2f(0.2f, start_y + 0.17f), 0.03f);
			if (opponent instanceof Damageable) {
				renderText(vg, "Health: " + ("" + cut_float(((Damageable) opponent).getCurrentHealth())),
						new Vector2f(0.2f, start_y + 0.2f), 0.03f);
				drawRect(0.2f, start_y + 0.25f, 0.11f, 0.03f, 0, 0, 0, 200, vg);

				size = ((Damageable) opponent).getCurrentHealth() * 0.11f;

				drawRect(0.201f, start_y + 0.251f, size - 0.002f, 0.028f, 200, 0, 0, 255, vg);
				drawRect(0.201f, start_y + 0.251f, Math.max(0, size - info_d[4] * 0.11f - 0.002f), 0.028f, 0, 200, 0,
						255, vg);
			}

		}

		@Override
		public boolean isInComponent(Vector2f vec) {
			return super.isInComponent(vec) && opponent != null;
		}

	}

	protected String cut_float(float in) {
		String out = in + "";
		return (out).substring(0, 3 + (out.length() > 3 ? 1 : 0));
	}

	@Override
	public void removed() {
		unitCombatInfo.opponent = null;
		removeDestinationSelectors();
		removeAttackBorder();
	}

	@Override
	public void newTeamTurn() {
		refresh();
	}

}
