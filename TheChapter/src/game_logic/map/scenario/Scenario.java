package game_logic.map.scenario;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import game_logic.map.MapGenerator;
import game_logic.map.cards.Card;
import game_logic.map.resources.Resource;
import game_logic.tile_object.BuildableByCity;
import game_logic.tile_object.TileObject;

public abstract class Scenario {

	// Objects
	private final LinkedList<Class<? extends TileObject>> objects = new LinkedList<Class<? extends TileObject>>();
	private final LinkedList<Class<? extends BuildableByCity>> buildableObjects = new LinkedList<Class<? extends BuildableByCity>>();
	private final LinkedList<Class<? extends Card>> cards = new LinkedList<Class<? extends Card>>();
	
	private final LinkedList<BuildableByCity> buildableObjectsPatterns = new LinkedList<BuildableByCity>();
	
	// Resources
	private final LinkedList<Resource> resources = new LinkedList<Resource>();

	// Map generator
	private MapGenerator mapGenerator;

	// Properties
	private ScenarioProperties properties;

	// Path
	private String localResourcesPath;

	public Scenario() {
	}

	public final void init() throws Exception {
		loadResources();
		
		properties = createProperties();
		mapGenerator = createGenerator();
		load();
	}

	protected abstract void load() throws Exception;

	protected abstract MapGenerator createGenerator();

	protected abstract ScenarioProperties createProperties();

	public abstract void loadResources() throws Exception;

	public abstract void cleanUp() throws Exception;

	@SuppressWarnings("unchecked")
	public final void addTileObjectType(Class<? extends TileObject> clazz) {
		if (!objects.contains(clazz)) {
			objects.add(clazz);
			if (BuildableByCity.class.isAssignableFrom(clazz)) {
				buildableObjects.add((Class<? extends BuildableByCity>) clazz);
				
				BuildableByCity build = null;
				try {
					try {
						build = (BuildableByCity) clazz.getDeclaredConstructor().newInstance();
					} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException
							| SecurityException e) {
						e.printStackTrace();
					}
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
				
				buildableObjectsPatterns.add(build);
			}
			if (Card.class.isAssignableFrom(clazz)) {
				cards.add((Class<? extends Card>) clazz);
			}
		}
	}

	public final void addResourceType(Resource res) {
		if (!resources.contains(res)) {
			resources.add(res);
		}
	}

	public final MapGenerator getMapGenerator() {
		return mapGenerator;
	}

	public final ScenarioProperties getProperties() {
		return properties;
	}

	public final String getLocalResourcesPath() {
		return localResourcesPath;
	}

	public final void setLocalResourcesPath(String localResourcesPath) {
		this.localResourcesPath = localResourcesPath;
	}

	public final LinkedList<BuildableByCity> getBuildableObjectsPatterns() {
		return buildableObjectsPatterns;
	}

}
