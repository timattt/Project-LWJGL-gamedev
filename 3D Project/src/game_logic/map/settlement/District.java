package game_logic.map.settlement;

import java.util.LinkedList;

import org.joml.Vector2f;

import game_logic.map.Map;
import game_logic.map.Team;
import game_logic.map.Tile;
import game_logic.map.TileSizeHandler;
import game_logic.map.decoration.Decoration;
import game_logic.storage.Textures;
import game_logic.tile_object.Combatable;
import game_logic.tile_object.Damageable;
import game_logic.tile_object.Movable;
import game_logic.tile_object.Observer;
import game_logic.tile_object.Resourceable;
import game_logic.tile_object.Resourceable.ResourceHeap;
import game_logic.tile_object.Teamable;
import game_logic.tile_object.TileObject;

public class District implements TileObject, Combatable, Teamable, Damageable, Observer {

	// Constants
	private static final float BASE_STRENGTH = 30f;

	// Home
	private Tile homeTile;
	private Map homeMap;

	// Decorations
	private static final Decoration[] decorations = new Decoration[0];

	// Settlement
	private Settlement settlement;

	// Building
	private LinkedList<Building> buildings = new LinkedList<Building>();

	// Strength
	private float strength = BASE_STRENGTH;

	// Health
	private float currentHealth = 1f;

	// Resources
	private ResourceHeap totalResourcesFromDistrict = new ResourceHeap(0) {
		@Override
		protected void fill() {
		}
	};

	public District(Settlement set) {
		this.settlement = set;
	}

	@Override
	public Team getMasterTeam() {
		return settlement.getMasterTeam();
	}

	@Override
	public void teamTurnFinished() {
		for (Combatable o : homeMap.getAllObjectsInRadius(Combatable.class, homeMap.getCoordinates(homeTile), 2)) {
			if (!o.isEnemy(this)) {
				heal(0.1f);
				return;
			}
		}
	}

	@Override
	public void teamTurnStarted() {
	}

	@Override
	public void giveToTeam(Team pl) {
	}

	@Override
	public void removeFromTeam() {
	}

	@Override
	public <T extends TileObject & Combatable> void attack(T opponent) {
	}

	@Override
	public <T extends TileObject & Combatable> void defend(T opponent) {
	}

	@Override
	public void doCombat(Combatable opponent) {
	}

	@Override
	public <T extends TileObject & Combatable> long getAttackTime() {
		return 0;
	}

	@Override
	public float getCourage() {
		return 0.1f;
	}

	@Override
	public float getEndurance() {
		return 0.1f;
	}

	@Override
	public int getRange() {
		return 1;
	}

	@Override
	public float getStrength() {
		return strength;
	}

	@Override
	public <T extends TileObject & Combatable> Vector2f[] getTargetsScalarCoordinates(T opp) {
		Vector2f[] vecs = new Vector2f[buildings.size() + 1];
		vecs[0] = new Vector2f();
		for (int i = 1; i < vecs.length; i++) {
			vecs[i] = buildings.get(i).getScalarCoordinates();
		}
		return vecs;
	}

	@Override
	public boolean isEnemy(Combatable obj) {
		return ((Teamable) obj).getMasterTeam() != getMasterTeam() && currentHealth > 0f;
	}

	@Override
	public <T extends TileObject & Combatable> void waitBeforeCombat(T opponent, Marker marker) {
		marker.ready();
	}

	@Override
	public void deleteFromMap(Map map) {
		map = null;
	}

	@Override
	public Decoration[] getDecorations() {
		return decorations;
	}

	@Override
	public Vector2f getDirection() {
		return TileObject.BASE_DIRECTION;
	}

	@Override
	public String getName() {
		return "District";
	}

	@Override
	public String getObjectInformation() {
		return "District";
	}

	@Override
	public <T extends Movable & TileObject> boolean isWalkable(T obj) {
		if (currentHealth <= 0f) {
			return true;
		}
		if (obj instanceof Teamable) {
			return ((Teamable) obj).getMasterTeam() == getMasterTeam();
		} else {
			return false;
		}
	}

	@Override
	public void turn_ended() {
	}

	@Override
	public void turn_started() {
	}

	@Override
	public void updateGraphics() {
	}

	@Override
	public float getDecorationHeight() {
		return TileSizeHandler.instance.getTileSize();
	}

	@Override
	public Map getHomeMap() {
		return homeMap;
	}

	@Override
	public Tile getHomeTile() {
		return homeTile;
	}

	@Override
	public void registerToMap(Map map) {
		this.homeMap = map;
	}

	@Override
	public void registerToTile(Tile homeTile) {
		this.homeTile = homeTile;
		homeMap.setTextureToTileMapMesh(Textures.GRAVEL_ROAD, homeTile);
	}

	@Override
	public void removeFromTile(Tile homeTile) {
		homeMap.removeTextureFromTileMapMesh(Textures.GRAVEL_ROAD, homeTile);
		this.homeTile = null;
	}

	public void recollectResources() {
		totalResourcesFromDistrict.clear();
		for (Resourceable t : homeTile.getAll(Resourceable.class)) {
			totalResourcesFromDistrict.merge(((Resourceable) t).getResources());
		}
	}

	public final ResourceHeap getTotalResourcesFromDistrict() {
		return totalResourcesFromDistrict;
	}

	public final LinkedList<Building> getAllBuildingsUnderDistrict() {
		return homeTile.getAll(Building.class);
	}

	@Override
	public float getCurrentHealth() {
		return currentHealth;
	}

	@Override
	public void damage(float val) {
		currentHealth -= val;
		if (currentHealth <= 0f) {
			settlement.destroyDistrict(this);
		}
	}

	@Override
	public void heal(float val) {
		currentHealth += val;
		currentHealth = Math.min(currentHealth, 1f);
	}

	@Override
	public boolean staticVisibility() {
		return true;
	}

	@Override
	public int getVisibleRange() {
		return 2;
	}

}
