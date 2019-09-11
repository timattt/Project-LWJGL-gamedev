package game_logic.map;

import java.util.LinkedList;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import game_logic.map.Tile.TileVisibility;
import game_logic.map.badges.Badge;
import game_logic.map.badges.BadgeRenderer;
import game_logic.map.cards.CardsRenderer;
import game_logic.map.decoration.Decoration;
import game_logic.map.scenario.Scenario;
import game_logic.storage.Meshes;
import game_logic.storage.Textures;
import game_logic.tile_object.Movable;
import game_logic.tile_object.Teamable;
import game_logic.tile_object.TileObject;
import game_logic.tile_object.moving.RouteCreator.RouteCreationStatus;
import graphicsSupport.Universe;
import graphicsSupport.effects.DirectionalLight;
import graphicsSupport.effects.SkyBox;
import graphicsSupport.particles.FadingParticlesGenerator;
import graphicsSupport.texture.Texture;

public class Map {

	// Size
	private final int width;
	private final int height;

	// Tiles
	private final Tile[][] tiles;

	// Decorations
	private final TileMapMesh tile_map;
	private final BadgeRenderer badgeRenderer = new BadgeRenderer();
	private final CardsRenderer cardsRenderer = new CardsRenderer();
	private final LinkedList<Decoration> decorations = new LinkedList<Decoration>();

	// Particles
	private final FadingParticlesGenerator[] particleGenerators = new FadingParticlesGenerator[3];
	public static final int PARTICLE_GEN_SMOKE = 0;
	public static final int PARTICLE_GEN_SPARKLES = 1;
	public static final int PARTICLE_GEN_EXPLOSION = 2;

	// Players
	private LinkedList<Team> teams = new LinkedList<Team>();
	private Team boundedTeam;

	// Lights
	private final Vector3f ambientLight = new Vector3f(0.1542963f, 0.1542963f, 0.1542963f);

	// Turns
	public int turnNumber = 0;

	// Name
	public final String name;

	// Scenario
	private final Scenario scenario;

	// Map
	private boolean valid = true;

	/**
	 * In this constructor map will be created from generated given from scenario.
	 * 
	 * @param scenario
	 * @throws Exception
	 */
	public Map(Scenario scenario) throws Exception {
		scenario.init();
		this.scenario = scenario;
		MapGenerator mapGen = this.scenario.getMapGenerator();
		mapGen.startGen();
		this.name = mapGen.getMapName();
		this.width = mapGen.getMapSize().x;
		this.height = mapGen.getMapSize().y;
		this.tiles = mapGen.getMapTiles();

		for (Team team : (this.teams = mapGen.getTeams())) {
			team.putToMap(this);
		}

		tile_map = new TileMapMesh(this);

		// Particles
		particleGenerators[PARTICLE_GEN_SMOKE] = new FadingParticlesGenerator(Meshes.PARTICLE, Textures.PARTICLE_SMOKE);
		particleGenerators[PARTICLE_GEN_SPARKLES] = new FadingParticlesGenerator(Meshes.PARTICLE,
				Textures.PARTICLE_SPARKLES);
		particleGenerators[PARTICLE_GEN_EXPLOSION] = new FadingParticlesGenerator(Meshes.PARTICLE,
				Textures.PARTICLE_EXPLOSION);

		// Setting particles generators
		for (int i = 0; i < particleGenerators.length; i++) {
			Universe.instance.addParticleGenerator(this.particleGenerators[i]);
		}

		Universe.instance.addExternalMesh(tile_map);
		Universe.instance.addExternalMesh(badgeRenderer);
		Universe.instance.addExternalMesh(cardsRenderer);

		// setting light
		Universe.instance.setAmbientLight(ambientLight);
		Universe.instance.setSpecularPower(9.817708f);

		DirectionalLight.instance.setIntensity(5.4809027f);
		DirectionalLight.instance.setColor(0.33628646f, 0.33628646f, 0.33628646f);

		// Skybox
		Universe.instance.setSkybox(new SkyBox(Meshes.SKYBOX1));

		mapGen.putObjects(this);
		mapGen.endGen();

		startTime = System.currentTimeMillis();
	}

	/**
	 * This method is invoked with every game update. It updates graphics in every
	 * tile object on the map.
	 */
	public final void updateGraphics() {

		// Updating tiles
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				tiles[x][y].updateGraphics();
			}
		}

		// updateDayNightCycle();
	}

	// Day night cycle
	private final long startTime;
	private static final long totalDayTime = 30l * 1000l;

	protected void updateDayNightCycle() {
		float angle = (float) Math.PI * 2f * (float) ((System.currentTimeMillis() - startTime) % totalDayTime)
				/ (float) totalDayTime;
		DirectionalLight.instance.getDirection().x = (float) Math.cos(angle);
		DirectionalLight.instance.getDirection().y = (float) Math.sin(angle);
	}

	/**
	 * This method is invoked at the end of every game turn. It ends turn in every
	 * tile object on the map. And also it moves objects that have long routes.
	 */
	public final void endTurn() {
		// Updating tiles
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				tiles[x][y].endTurn();
			}
		}
	}

	/**
	 * This method is invoked at the start of every game turn. It starts turn in
	 * every tile object on the map.
	 */
	public final void turn_started() {
		// Updating tiles
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				tiles[x][y].turn_started();
			}
		}
	}

	/**
	 * This method gives width of the map in tiles.
	 * 
	 * @return : Width.
	 */
	public final int getWidth() {
		return width;
	}

	/**
	 * This method gives height of the map in tiles.
	 * 
	 * @return : Height.
	 */
	public final int getHeight() {
		return height;
	}

	/**
	 * This method gives length between two tiles on the map. The length is
	 * calculated by formula max((vec_t1 - vec_t).x, (vec_t1 - vec_t).y).
	 * 
	 * @param t
	 *            : First tile.
	 * @param t1
	 *            : Second tile.
	 * @return : Length.
	 */
	public final int getLength(Tile t, Tile t1) {
		Vector2i dir = new Vector2i(this.getCoordinates(t));
		Vector2i dir1 = new Vector2i(this.getCoordinates(t1));
		dir.negate();
		dir1.add(dir);
		return Math.max(Math.abs(dir1.x), Math.abs(dir1.y));
	}

	/**
	 * This method gives length between two coordinates on the map. The length is
	 * calculated by formula max((vec_t1 - vec_t).x, (vec_t1 - vec_t).y).
	 * 
	 * @param vec1
	 *            : First coordinate.
	 * @param vec2
	 *            : Second coordinate.
	 * @return : Length.
	 */
	public final int getLength(Vector2i vec1, Vector2i vec2) {
		Vector2i dir = new Vector2i(vec1);
		Vector2i dir1 = new Vector2i(vec2);
		dir1.negate();
		dir.add(dir1);
		return Math.max(Math.abs(dir.x), Math.abs(dir.y));
	}

	/**
	 * This method just return tiles array.
	 * 
	 * @return : Tiles array.
	 */
	public final Tile[][] getTiles() {
		return tiles;
	}

	/**
	 * This method gives tile coordinate on this map. If this map is not contains
	 * given tile then null will be returned.
	 * 
	 * @param tile
	 *            : Tile for calculating coordinates.
	 * @return : Given tile coordinates.
	 */
	public final Vector2i getCoordinates(Tile tile) {
		for (int x = 0; x < width; x++) {
			for (int z = 0; z < height; z++) {
				if (tiles[x][z] == tile) {
					return new Vector2i(x, z);
				}
			}
		}
		return null;
	}

	/**
	 * This method gives tile from given coordinates.
	 * 
	 * @param vec
	 *            : Coordinates to give tile from.
	 * @return : Tile.
	 */
	public final Tile getTile(Vector2i vec) {
		return tiles[vec.x][vec.y];
	}

	/**
	 * This method gives tile from coordinates (X, Z).
	 * 
	 * @param x
	 *            : X coordinate.
	 * @param z
	 *            : Z coordinate.
	 * @return : Tile.
	 */
	public final Tile getTile(int x, int z) {
		return tiles[x][z];
	}

	/**
	 * This method gives tile for witch the given coordinate is inside holding
	 * points.
	 * 
	 * @param vec
	 *            : Vector.
	 * @return : Tile.
	 */
	public final Tile getTile(Vector3f vec) {
		try {
			return tiles[(int) ((float) (vec.x / (float) (width * TileSizeHandler.instance.getTileSize()))
					* width)][(int) ((float) (vec.z / (float) (height * TileSizeHandler.instance.getTileSize()))
							* height)];
		} catch (Exception e) {
			throw new IllegalStateException("Vector is not on the tile map!");
		}
	}

	/**
	 * This method sets texture for given tile on that map.
	 * 
	 * @param text
	 *            : Texture for tile.
	 * @param t
	 *            : Tile to set texture on.
	 */
	public final void setTextureToTileMapMesh(Texture text, Tile t) {
		Vector2i vec = getCoordinates(t);
		tile_map.setTextureForTile(vec.x, vec.y, text);
	}

	/**
	 * This method sets texture for given tile on that map.
	 * 
	 * @param text
	 *            : Texture for tile.
	 * @param t
	 *            : Tile to set texture on.
	 * @param angle
	 *            : angle to rotate texture.
	 */
	public final void setTextureToTileMapMesh(Texture text, Tile t, int angle) {
		Vector2i vec = getCoordinates(t);
		tile_map.setTextureForTile(vec.x, vec.y, text, angle);
	}

	/**
	 * This method removes texture from tile map.
	 * 
	 * @param text
	 *            : Texture to remove.
	 * @param t
	 *            : Tile to remove from.
	 */
	public final void removeTextureFromTileMapMesh(Texture text, Tile t) {
		Vector2i vec = getCoordinates(t);
		tile_map.removeTextureFromTile(vec.x, vec.y, text);
	}

	/**
	 * This method checks if given object is on this map.
	 * 
	 * @param obj
	 *            : Object to check.
	 * @return : True if object is on the map.
	 */
	public final boolean containsOnMap(TileObject obj) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (tiles[x][y].containsTileObject(obj)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This method registers object to given coordinates (x, y). The process has
	 * these steps: 1. Invoke (TileObject) abstract method (registerToMap). 2. Add
	 * to tile list. 3. Invoke (TileObject) abstract method (registerToTile). 4.
	 * Prepare decorations. All actions are synchronized.
	 * 
	 * @param obj
	 *            : Object to add.
	 * @param x
	 *            : X coordinate.
	 * @param y
	 *            : Z coordinate.
	 */
	public final synchronized void registerTileObject(TileObject obj, int x, int y) {
		// Console.println("Registered tile object " + obj.getName() + " to [" + x + ",
		// " + y + "]");
		obj.registerToMap(this);
		tiles[x][y].addTileObject(obj);
		if (!obj.mustBePrepared()) {
			reloadDecorations(obj);
		} else {
			reload_prepare_Decorations(obj);
		}
	}

	/**
	 * This method registers object to given coordinates (x, y). The process has
	 * these steps: 1. Invoke (TileObject) abstract method (registerToMap). 2. Add
	 * to tile list. 3. Invoke (TileObject) abstract method (registerToTile). 4.
	 * Prepare decorations. All actions are synchronized.
	 * 
	 * @param obj
	 *            : Object to add.
	 * @param x
	 *            : X coordinate.
	 * @param y
	 *            : Z coordinate.
	 */
	public final synchronized <T extends TileObject & Teamable> void registerTileObject(T obj, int x, int y,
			Team team) {
		// Console.println("Registered tile object " + obj.getName() + " to [" + x + ",
		// " + y + "]");
		team.addTeamable(obj);
		obj.registerToMap(this);
		tiles[x][y].addTileObject(obj);
		if (!obj.mustBePrepared()) {
			reloadDecorations(obj);
		} else {
			reload_prepare_Decorations(obj);
		}

	}

	/**
	 * This method moves object to the given position. If position is blocked by
	 * something then it will change status to BLOCKED and move object to nearest
	 * tile by the route. Object will move even the start position is blocked; If
	 * the route is too big then it will be stored in map array and at the next turn
	 * unit will move to the destination automatically; If object already have long
	 * route then it will be deleted and if required replaced with new.
	 * <p>
	 * Additional information is in the Route status variable.
	 * 
	 * @param obj
	 *            : Object to move;
	 * @param end_pos
	 *            : Position to move on;
	 * @param status
	 *            : Information about moving;
	 */
	public final synchronized <T extends TileObject & Movable & Teamable> void moveObject(T obj, Vector2i end_pos,
			RouteCreationStatus status) {
		// Console.println("Moved object " + obj.getName() + " to [" + end_pos.x + ", "
		// + end_pos.y + "]");

		// Generating path
		Tile[] dest = cutPath(obj, end_pos, status);

		// Checking if the route is too small
		if (dest.length < 2) {
			return;
		}

		// Removing object from start tile
		Tile start = obj.getHomeTile();
		start.removeTileObject(obj);

		// Given path
		for (int i = 1; i < dest.length - 1; i++) {
			dest[i].addTileObject(obj);
			dest[i].removeTileObject(obj);
		}

		dest[dest.length - 1].addTileObject(obj);

		obj.move(dest);

		// If the route is too big then it will be continued at the next
		// turn
		if (obj.getMasterTeam().containsMovable(obj)) {
			obj.getMasterTeam().removeMovable(obj);
		}
		if (!(this.getCoordinates(obj.getHomeTile()).x == end_pos.x
				&& this.getCoordinates(obj.getHomeTile()).y == end_pos.y)
				&& status.status == RouteCreationStatus.status_OK) {
			obj.getMasterTeam().putMovable(obj, end_pos);
		}

	}

	/**
	 * This method cuts path for (moveObject) method by objects current movement
	 * points.
	 * 
	 * @param obj
	 *            : Movable object to cut path for.
	 * @param dest
	 *            : Path destination.
	 * @param status
	 *            : Route creating status.
	 * @return : Cutted path array.
	 */
	private final <T extends TileObject & Movable> Tile[] cutPath(T obj, Vector2i dest, RouteCreationStatus status) {
		Tile[] path = Movable.findBestRoute(obj, dest, status);

		Tile[] result = new Tile[Math.min(obj.getCurrentMovePoints() + 1, path.length)];
		if (result.length == 1) {
			return new Tile[0];
		}
		for (int i = 0; i < result.length; i++) {
			result[i] = path[i];
		}

		return result;
	}

	/**
	 * This method removes tile object and all its decorations from this map. The
	 * process is doing by the following steps: 1. Find and remove all decorations.
	 * 2. Invoke abstract (TileObject) method (removeFromTile). 3. Remove from tile
	 * list. 4. Invoke abstract (TileObject) method (removeFromMap). All actions are
	 * synchronized.
	 * 
	 * @param obj
	 *            : Object to remove.
	 */
	public final synchronized void removeTileObject(TileObject obj) {
		if (!containsOnMap(obj)) {
			return;
		}

		obj.getHomeTile().removeTileObject(obj);
		obj.deleteFromMap(this);
		for (Decoration dec : findAllObjectDecorations(obj)) {
			removeDecoration(dec);
		}

		if (obj instanceof Teamable && ((Teamable) obj).getMasterTeam() != null) {
			((Teamable) obj).getMasterTeam().removeTeamable((Teamable) obj);
		}
	}

	/**
	 * This method adds team to this map. It adds it to map teams list.
	 * 
	 * @param pl
	 *            : Player to add.
	 */
	public final void addTeam(Team pl) {
		teams.add(pl);
	}

	/**
	 * This method removes team from this map. It removes it from map teams list.
	 * 
	 * @param pl
	 *            : Player to remove.
	 */
	public final void removeTeam(Team pl) {
		teams.remove(pl);
	}

	/**
	 * This method finds all object given object decorations by sorting out all
	 * decorations.
	 * 
	 * @param obj
	 *            : Object whose decorations must be found.
	 * @return : List of objects decorations.
	 */
	private final LinkedList<Decoration> findAllObjectDecorations(TileObject obj) {
		LinkedList<Decoration> result = new LinkedList<Decoration>();
		for (Decoration dec : decorations) {
			if (dec.getObject() == obj) {
				result.add(dec);
			}
		}
		return result;
	}

	/**
	 * This method adds decoration to map decorations list and to universe screen
	 * objects list and then refresh decoration itself.
	 * 
	 * @param dec
	 *            : Decoration to work on.
	 */
	public final void addDecoration(Decoration dec) {
		if (decorations.contains(dec)) {
			return;
		}
		dec.refresh();
		decorations.add(dec);
		checkDecorationByVisibility(dec);
	}

	/**
	 * This method will return all near stand objects.
	 * 
	 * @param t
	 *            : Center tile.
	 * @param cl
	 *            : Objects type.
	 * @return : List of objects.
	 */
	public LinkedList<TileObject> getNearestTileObjectsType(Tile t, Class<?> cl) {
		int min = Integer.MAX_VALUE;
		LinkedList<TileObject> objs = new LinkedList<TileObject>();

		for (int x = 0; x < width; x++) {
			for (int z = 0; z < height; z++) {
				int length = getLength(t, tiles[x][z]);
				for (TileObject o : tiles[x][z].getObjects()) {
					if (cl.isInstance(o) && length <= min) {
						objs.add(o);
						min = length;
					}
				}
			}
		}

		return objs;
	}

	/**
	 * This method checks if map has any tile object type in the map radius of the
	 * given center.
	 * 
	 * @param clazz
	 * @param center
	 * @param rad
	 * @return
	 */
	public <T extends TileObject> boolean hasTypeInRadius(Class<T> clazz, Vector2i center, int rad) {
		Vector2i tile = new Vector2i();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				tile.set(x, y);
				if (getLength(center, tile) <= rad && tiles[x][y].containsType(clazz)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 
	 * @param clazz
	 * @param center
	 * @param rad
	 * @return
	 */
	public <T> LinkedList<T> getAllObjectsInRadius(Class<T> clazz, Vector2i center, int rad) {
		Vector2i tile = new Vector2i();
		LinkedList<T> objs = new LinkedList<T>();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				tile.set(x, y);
				if (getLength(center, tile) <= rad) {
					for (T obj : tiles[x][y].getAll(clazz)) {
						objs.add(obj);
					}
				}
			}
		}

		return objs;
	}

	/**
	 * This method removes decoration from map and universe decorations lists.
	 * 
	 * @param dec
	 *            : Decoration that will be removed.
	 */
	public final void removeDecoration(Decoration dec) {
		synchronized (Universe.instance) {
			decorations.remove(dec);
			Universe.instance.deleteObject(dec);
		}
	}

	/**
	 * This method firstly delete all decorations owned by given object and then
	 * collect them from object and load them again.
	 * 
	 * Do not use this method recently!
	 * 
	 * @param obj
	 *            : Object to take decorations from.
	 */
	public final void reloadDecorations(TileObject obj) {
		// Deleting old decorations
		for (Decoration dec : findAllObjectDecorations(obj)) {
			removeDecoration(dec);
		}

		// Adding new decorations
		for (Decoration dec : obj.getDecorations()) {
			if (dec == null) {
				continue;
			}

			addDecoration(dec);
		}
	}

	/**
	 * This method firstly delete all decorations owned by given object and then
	 * collect them from object, load and PREPARE (invoke decoration (prepare)
	 * method) them again.
	 * 
	 * Do not use this method recently!
	 * 
	 * @param obj
	 *            : Object to take decorations from.
	 */
	public void reload_prepare_Decorations(TileObject obj) {
		// Deleting old decorations
		for (Decoration dec : findAllObjectDecorations(obj)) {
			removeDecoration(dec);
		}

		// Adding new decorations
		for (Decoration dec : obj.getDecorations()) {
			if (dec == null) {
				continue;
			}
			dec.prepare(obj.getHomeTile());
			addDecoration(dec);
		}
	}

	/**
	 * This method adds badge to renderer.
	 * 
	 * @param badge
	 *            : Badge that will be rendered.
	 */
	public void addBadge(Badge badge) {
		badgeRenderer.add(badge);
	}

	/**
	 * This method removes badge from renderer.
	 * 
	 * @param badge
	 *            : Badge that will be removed.
	 */
	public void removeBadge(Badge badge) {
		badgeRenderer.remove(badge);
	}

	/**
	 * This method must be invoked when this map is terminating. It deletes all
	 * decorations and tile map.
	 * 
	 * @throws Exception
	 */
	public final void cleanup_remove() throws Exception {
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[x].length; y++) {
				for (int i = 0; i < tiles[x][y].getObjects().size(); i++) {
					tiles[x][y].getObjects().get(i).removeFromTile(tiles[x][y]);
					tiles[x][y].getObjects().get(i).deleteFromMap(this);
				}
			}
		}

		tile_map.cleanup();
		Universe.instance.setSkybox(null);

		for (Decoration dec : decorations) {
			Universe.instance.deleteObject(dec);
		}

		Universe.instance.removeExternalMesh(badgeRenderer);
		Universe.instance.removeExternalMesh(tile_map);

		scenario.cleanUp();
		valid = false;
	}

	/**
	 * This method gives teams list.
	 * 
	 * @return : List of teams on this map.
	 */
	public final LinkedList<Team> getTeams() {
		return teams;
	}

	/**
	 * This method creates and adds new particle to given generator.
	 * 
	 * @param genId
	 *            : Id of the particle generator.
	 * @param pos
	 *            : Position where particle must be.
	 * @param lifeTime
	 *            : Time that particle will be in memory.
	 */
	public void addParticle(int genId, Vector3f pos, long startTime, long lifeTime, float scalStart, float scalEnd) {
		particleGenerators[genId].addParticle(pos, startTime, lifeTime, scalStart, scalEnd);
	}

	public final Vector3f getAmbientLight() {
		return ambientLight;
	}

	public final SkyBox getSkybox() {
		return Universe.instance.getSkybox();
	}

	public final TileMapMesh getTileMapMesh() {
		return tile_map;
	}

	public final Scenario getScenario() {
		return scenario;
	}

	public final void boundToTeam(Team team) {
		boundedTeam = team;
	}

	public final Team getBoundedTeam() {
		return boundedTeam;
	}

	private void checkDecorationByVisibility(Decoration dec) {
		if (boundedTeam == null) {
			return;
		}

		synchronized (Universe.instance) {

			boolean contains = Universe.instance.contains(dec);

			Tile tile = getTile(dec.getPosition());

			Vector2i coords = getCoordinates(tile);
			TileVisibility mode = boundedTeam.getVisibilityMap()[coords.x][coords.y];

			// If hidden
			if (mode == TileVisibility.HIDDEN) {
				if (contains) {
					Universe.instance.deleteObject(dec);
				}
				return;
			}

			// If object is not visible static then
			if (mode == TileVisibility.INVISIBLE && !dec.getObject().staticVisibility()) {
				if (contains) {
					Universe.instance.deleteObject(dec);
				}
				return;
			}

			// If not
			if (!contains && decorations.contains(dec)) {
				Universe.instance.addObject(dec);
			}

			// If invisible
			if (mode == TileVisibility.INVISIBLE) {
				dec.setColorScalar(0.5f);
				return;
			}

			// If visible
			if (mode == TileVisibility.VISIBLE) {
				dec.setColorScalar(1f);
				return;
			}

		}
	}

	public final boolean isValid() {
		return valid;
	}

	public final void updateAllVisibility() throws Exception {
		boundedTeam.refreshVisibleArea();

		for (int i = 0; i < decorations.size(); i++) {
			checkDecorationByVisibility(decorations.get(i));
		}
	}

	public final CardsRenderer getCardsRenderer() {
		return cardsRenderer;
	}

	public final float projectY(Vector3f vec) {
		Tile tile = getTile(vec);
		return tile.projectY(vec);
	}

	public final boolean isOnMap(Vector3f vec, float epsilon) {
		Tile tile = getTile(vec);
		return tile.isOnTile(vec, epsilon);
	}

	public final Quaternionf createRotation(Vector3f vec) {
		Tile tile = getTile(vec);
		return tile.createQuaternion(vec);
	}

	public final Quaternionf createRotation(Tile tile, Vector2f vec) {
		return tile.createQuaternion(vec);
	}

}
