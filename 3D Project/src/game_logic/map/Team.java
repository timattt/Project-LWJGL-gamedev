package game_logic.map;

import java.util.HashMap;
import java.util.LinkedList;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

import engine.Engine;
import game_logic.GameOptions;
import game_logic.map.Tile.TileVisibility;
import game_logic.map.cards.Card;
import game_logic.map.player.Player;
import game_logic.tile_object.BuildableByCity;
import game_logic.tile_object.Controllable;
import game_logic.tile_object.Movable;
import game_logic.tile_object.Observer;
import game_logic.tile_object.Teamable;
import game_logic.tile_object.TileObject;
import game_logic.tile_object.moving.RouteCreator.RouteCreationStatus;

public class Team {

	// Objects
	protected final LinkedList<Teamable> teamObjects = new LinkedList<Teamable>();
	protected final LinkedList<Controllable> controllableObjects = new LinkedList<Controllable>();
	protected final LinkedList<Observer> observers = new LinkedList<Observer>();

	// Cards
	protected final LinkedList<Card> cards = new LinkedList<Card>();

	// Movable
	private final HashMap<Movable, Vector2i> objects_moves = new HashMap<Movable, Vector2i>();

	// Map
	protected Map map;
	protected volatile TileVisibility[][] visibilityMap;

	// Color
	protected Vector3f player_color;

	// Name
	protected String name;

	// Players who controls this team
	protected LinkedList<Player> players = new LinkedList<Player>();

	public Team() {
		player_color = new Vector3f((float) Math.random(), (float) Math.random(), (float) Math.random());
	}

	/**
	 * Use only in map class.
	 * 
	 * @param t
	 */
	protected final void addTeamable(Teamable t) {
		teamObjects.add(t);
		if (t instanceof Controllable) {
			controllableObjects.add((Controllable) t);
		}
		if (t instanceof Observer) {
			observers.add((Observer) t);
		}
		t.giveToTeam(this);
	}

	public final void removeTeamable(Teamable t) {
		teamObjects.remove(t);
		if (t instanceof Controllable) {
			controllableObjects.remove((Controllable) t);
		}
		if (t instanceof Observer) {
			observers.remove((Observer) t);
		}
		t.removeFromTeam();
	}

	public final Vector3f getPlayer_color() {
		return player_color;
	}

	public final Team setName(String name) {
		this.name = name;
		return this;
	}

	public final void turnStarted() {
		for (int i = 0; i < teamObjects.size(); i++) {
			teamObjects.get(i).teamTurnStarted();
		}
	}

	public final void turnFinished() {
		// Moving
		while (true) {
			try {
				synchronized (objects_moves) {
					for (Movable key : objects_moves.keySet()) {
						Vector2i vec = map.getCoordinates(((TileObject) key).getHomeTile());
						if (vec.x == objects_moves.get(key).x && vec.y == objects_moves.get(key).y) {
							objects_moves.remove(key);
							continue;
						}
						RouteCreationStatus status = new RouteCreationStatus();
						map.moveObject((Teamable & TileObject & Movable) key, this.objects_moves.get(key), status);
						if (status.status == RouteCreationStatus.status_blocked) {
							objects_moves.remove(key);
							continue;
						}
					}
				}
			} catch (Exception e) {
				continue;
			}
			break;
		}

		for (int i = 0; i < teamObjects.size(); i++) {
			teamObjects.get(i).teamTurnFinished();
		}

	}

	public String getName() {
		return name;
	}

	public Vector3i getPlayer_color_i() {
		return new Vector3i((int) (this.player_color.x * 255f), (int) (this.player_color.y * 255f),
				(int) (this.player_color.z * 255f));
	}

	public Vector2i putMovable(Movable key, Vector2i value) {
		return objects_moves.put(key, value);
	}

	public void removeMovable(Movable key) {
		objects_moves.remove(key);
	}

	public boolean containsMovable(Movable obj) {
		return objects_moves.containsKey(obj);
	}

	public Vector2i getMovableDestination(Movable obj) {
		return objects_moves.get(obj);
	}

	/**
	 * DO NOT USE THIS!
	 * 
	 * @param player
	 */
	public final void addPlayerInTeam(Player player) {
		players.add(player);
	}

	public final Map getMap() {
		return map;
	}

	public final void putToMap(Map map) {
		this.map = map;

		visibilityMap = new TileVisibility[map.getWidth()][map.getHeight()];
		for (int x = 0; x < map.getWidth(); x++) {
			for (int y = 0; y < map.getHeight(); y++) {
				visibilityMap[x][y] = TileVisibility.HIDDEN;
			}
		}
	}

	public final void doTurn() {
		for (Player pl : players) {
			pl.doTurn(this);
		}
	}

	public final LinkedList<Player> getPlayers() {
		return players;
	}

	public final LinkedList<Teamable> getTeamObjects() {
		return teamObjects;
	}

	public final LinkedList<Controllable> getControllableObjects() {
		return controllableObjects;
	}

	public final boolean projectIsPossible(BuildableByCity project) {
		return true;
	}

	public final void addCard(Card card) {
		cards.add(card);
	}

	public final void placeCard(Card card, int x, int y) {
		cards.remove(card);
		map.registerTileObject(card, x, y, this);
	}

	public final LinkedList<Card> getCards() {
		return cards;
	}

	protected final void refreshVisibleArea() {
		for (int x = 0; x < map.getWidth(); x++) {
			A: for (int y = 0; y < map.getHeight(); y++) {
				Tile tile = map.getTile(x, y);
				if (((GameOptions) Engine.getExternalOptions()).isDisableVisibility()) {
					visibilityMap[x][y] = TileVisibility.VISIBLE;
					continue;
				}
				for (Observer o : observers) {
					int range = map.getLength(tile, ((TileObject) o).getHomeTile());
					if (range <= o.getVisibleRange()) {
						visibilityMap[x][y] = TileVisibility.VISIBLE;
						continue A;
					}
				}
				if (visibilityMap[x][y] != TileVisibility.HIDDEN) {
					visibilityMap[x][y] = TileVisibility.INVISIBLE;
				}

			}
		}
	}

	public final TileVisibility[][] getVisibilityMap() {
		return visibilityMap;
	}

}
