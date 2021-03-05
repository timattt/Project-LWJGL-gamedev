/**
 * 
 */
package game_logic.map.cards;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import game_logic.graphics.StrategyCamera;
import game_logic.map.Map;
import game_logic.map.Team;
import game_logic.map.Tile;
import game_logic.map.decoration.Decoration;
import game_logic.storage.Meshes;
import game_logic.tile_object.Movable;
import game_logic.tile_object.Teamable;
import game_logic.tile_object.TileObject;
import graphicsSupport.texture.Texture;

/**
 * @author timat
 *
 */
public abstract class Card implements TileObject, Teamable {

	// Decorations
	protected final Decoration[] decorations = new Decoration[] { new Decoration(Meshes.CARD[1], this) };

	private final Texture texture;
	
	// Falling
	private Vector3f acceleration;
	private final Vector3f speed = new Vector3f();
	private float y_max;
	private boolean fall;
	private long startTime;

	// Home
	private Tile homeTile;
	private Map homeMap;

	// Team
	private Team masterTeam;

	public Card(Texture texture) {
		this.texture = texture;
	}
	
	public abstract void putted();
	
	public abstract boolean canBePutted(Tile tile);

	public abstract String getCardName();

	@Override
	public Team getMasterTeam() {
		return masterTeam;
	}

	@Override
	public void teamTurnFinished() {
	}

	@Override
	public void teamTurnStarted() {
	}

	@Override
	public void giveToTeam(Team pl) {
		masterTeam = pl;
	}

	@Override
	public void removeFromTeam() {
		masterTeam = null;
	}

	@Override
	public void deleteFromMap(Map map) {
		homeMap = null;
	}

	@Override
	public Decoration[] getDecorations() {
		return decorations;
	}

	@Override
	public Vector2f getDirection() {
		return BASE_DIRECTION;
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
	public String getName() {
		return "Card";
	}

	@Override
	public String getObjectInformation() {
		return null;
	}

	@Override
	public <T extends Movable & TileObject> boolean isWalkable(T obj) {
		return false;
	}

	@Override
	public void registerToMap(Map map) {
		homeMap = map;
	}

	@Override
	public void registerToTile(Tile homeTile) {
		this.homeTile = homeTile;

		Vector3f startPos = new Vector3f(homeTile.center);

		acceleration = new Vector3f(this.homeTile.globalNormal);
		acceleration.normalize();
		acceleration.mul(10f);
		
		acceleration.mul(StrategyCamera.instance.getPosition().y - homeTile.center.y);
		
		startPos.add(acceleration);

		acceleration.negate();
		acceleration.normalize();
		acceleration.mul(10000f);

		speed.zero();

		y_max = homeTile.center.y;

		decorations[0].setPosition(startPos);
		homeMap.getCardsRenderer().add(this);
		decorations[0].rotate(homeTile.globalRotation);
		startTime = System.currentTimeMillis();

		fall = true;
	}

	@Override
	public void removeFromTile(Tile homeTile) {
		this.homeTile = null;

		homeMap.getCardsRenderer().remove(this);
	}

	@Override
	public void turn_ended() {
	}

	@Override
	public void turn_started() {
	}

	@Override
	public void updateGraphics() {
		if (fall) {
			long deltaTime = System.currentTimeMillis() - startTime;

			Vector3f newa = new Vector3f(acceleration);
			newa.normalize();
			newa.mul(0.0001f);
			newa.mul(deltaTime);
			speed.add(newa);
			decorations[0].getPosition().add(speed);

			if (decorations[0].getPosition().y < y_max) {
				speed.negate();
				decorations[0].getPosition().set(homeTile.center);
				decorations[0].updateModelMatrix();
				fall = false;
				putted();
				return;
			}
			decorations[0].updateModelMatrix();
			startTime = System.currentTimeMillis();
		}
	}

	@Override
	public float getDecorationHeight() {
		return 3f;
	}

	@Override
	public boolean staticVisibility() {
		return false;
	}

	public final Texture getTexture() {
		return texture;
	}

	public Matrix4f getModelMatrix() {
		return decorations[0].getModelMatrix();
	}

	@Override
	public boolean mustBePrepared() {
		return false;
	}
	
}
