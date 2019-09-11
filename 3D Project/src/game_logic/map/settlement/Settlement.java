package game_logic.map.settlement;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import org.joml.Vector2f;
import org.joml.Vector2i;

import game_logic.gui.controllers.TileObjectController;
import game_logic.map.Map;
import game_logic.map.Team;
import game_logic.map.Tile;
import game_logic.map.TileSizeHandler;
import game_logic.map.decoration.Decoration;
import game_logic.tile_object.BuildableByCity;
import game_logic.tile_object.Controllable;
import game_logic.tile_object.Movable;
import game_logic.tile_object.Resourceable.ResourceHeap;
import game_logic.tile_object.Teamable;
import game_logic.tile_object.TileObject;

public class Settlement implements TileObject, Teamable, Controllable {

	// Home & master
	private Tile homeTile;
	private Map homeMap;
	private Team masterTeam;
	
	// Districts
	private LinkedList<District> districts = new LinkedList<District>();

	// Total resources
	private ResourceHeap totalResources = new ResourceHeap(0) {
		@Override
		protected void fill() {
		}
	};

	// Decorations
	private Decoration[] decorations = new Decoration[0];

	// Population
	private int population = 10;

	// name
	private String settlementName = "unnamed";

	// Main resources income
	private int productionIncome;
	private int livelihoodIncome;

	// Building projects
	private LinkedList<BuildingProject> projectsQueue = new LinkedList<BuildingProject>();

	public Settlement() {
	}

	@Override
	public TileObjectController getGui() {
		return TileObjectController.settlement_controller;
	}

	@Override
	public boolean canControl(Team player) {
		return masterTeam == player;
	}

	@Override
	public Team getMasterTeam() {
		return masterTeam;
	}

	@Override
	public void teamTurnFinished() {
		refresh();
		addProductionToBuildProject();
	}

	@Override
	public void teamTurnStarted() {
		refresh();
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
	public Map getHomeMap() {
		return homeMap;
	}

	@Override
	public Tile getHomeTile() {
		return homeTile;
	}

	@Override
	public String getName() {
		return "Settlement";
	}

	@Override
	public String getObjectInformation() {
		return "Settlement " + settlementName;
	}

	@Override
	public void registerToMap(Map map) {
		homeMap = map;
	}

	@Override
	public void registerToTile(Tile homeTile) {
		this.homeTile = homeTile;
		Vector2i coords = homeMap.getCoordinates(homeTile);
		addDistrict(coords.x, coords.y);
		addBuilding(this.homeMap.getScenario().getProperties().getCityMainBuilding(), coords.x, coords.y);
	}
	
	public void destroyDistrict(District dist) {
		districts.remove(dist);
		homeMap.removeTileObject(dist);
		if (districts.size() == 0) {
			killSettlement();
		}
	}

	@Override
	public void removeFromTile(Tile homeTile) {
		this.homeTile = null;

		for (District d : districts) {
			homeMap.removeTileObject(d);
		}

		districts.clear();
		totalResources.clear();
	}

	@Override
	public void turn_ended() {
		refresh();
	}

	@Override
	public void turn_started() {
		refresh();
	}

	@Override
	public void updateGraphics() {
	}

	@Override
	public float getDecorationHeight() {
		return TileSizeHandler.instance.getTileSize();
	}

	public final boolean canAddDistrict(Tile tile) {
		return population > districts.size() && !tile.containsType(District.class);
	}

	public final boolean canAddBuildable(BuildableByCity b, int x, int y) {
		return !homeMap.getTile(x, y).containsType(b.getClass());
	}

	public final void addDistrict(int x, int y) {
		District dist = new District(this);
		homeMap.registerTileObject(dist, x, y, this.masterTeam);
		districts.add(dist);
	}

	private void addBuilding(Class<? extends Building> clazz, int x, int y) {
		Building build = null;
		try {
			try {
				build = clazz.getDeclaredConstructor().newInstance();
			} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				e.printStackTrace();
			}
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

		homeMap.registerTileObject(build, x, y);
	}

	private void addBuildable(BuildableByCity proj, int x, int y) {
		homeMap.registerTileObject((TileObject) proj, x, y);
	}

	public void refresh() {
		refreshResources();
		refreshIncome();
		refreshPopulation();
	}

	private void refreshPopulation() {
		
	}
	
	private void refreshResources() {
		totalResources.clear();
		for (District d : districts) {
			d.recollectResources();
			totalResources.merge(d.getTotalResourcesFromDistrict());
		}
	}

	private void refreshIncome() {
		productionIncome = totalResources
				.getResourceQuanity(this.homeMap.getScenario().getProperties().getCitiesConstructionalResource());
		livelihoodIncome = totalResources
				.getResourceQuanity(this.homeMap.getScenario().getProperties().getCitiesLivelihoodResource());
	}

	private void addProductionToBuildProject() {
		if (!this.projectsQueue.isEmpty()) {
			this.projectsQueue.getFirst().addProduction();
			if (this.projectsQueue.getFirst().complete >= 1f) {
				projectsQueue.removeFirst().projectFinished();
			}
		}
	}

	public final ResourceHeap getTotalResources() {
		return totalResources;
	}

	public final int getPopulation() {
		return population;
	}

	public final String getSettlementName() {
		return settlementName;
	}

	public final void setSettlementName(String settlementName) {
		this.settlementName = settlementName;
	}

	public final LinkedList<District> getDistricts() {
		return districts;
	}

	public void addProject(Class<? extends BuildableByCity> proj, int x, int y) {
		projectsQueue.addLast(new BuildingProject(proj, x, y));
	}

	public final int getProductionIncome() {
		return productionIncome;
	}

	public final int getLivelihoodIncome() {
		return livelihoodIncome;
	}

	public final BuildableByCity getProject() {
		return this.projectsQueue.isEmpty() ? null : this.projectsQueue.getFirst().project;
	}

	public final float getComplete() {
		return this.projectsQueue.isEmpty() ? 0f : this.projectsQueue.getFirst().complete;
	}

	public final LinkedList<BuildingProject> getProjectsQueue() {
		return projectsQueue;
	}

	public void killSettlement() {
		homeMap.removeTileObject(this);
	}
	
	public int getProjectCompletionDuration() {
		return (int) (projectsQueue.isEmpty() ? -1 : (projectsQueue.getFirst().getProject().getBaseCost() - projectsQueue.getFirst().complete * projectsQueue.getFirst().getProject().getBaseCost()) / productionIncome);
	}
	
	public class BuildingProject {

		private final BuildableByCity project;
		private final Vector2i projectPosition;
		private float complete = 0f;

		private TileObject mark;
		
		public BuildingProject(Class<? extends BuildableByCity> proj, int x, int y) {
			BuildableByCity build = null;
			try {
				try {
					build = proj.getDeclaredConstructor().newInstance();
				} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException
						| SecurityException e) {
					e.printStackTrace();
				}
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}

			project = build;
			projectPosition = new Vector2i(x, y);

			try {
				mark = homeMap.getScenario().getProperties().getCitiesBuildingProcessMarker().getDeclaredConstructor().newInstance();
			} catch (Exception e) {
			}

			homeMap.registerTileObject(mark, x, y);
		}

		private void projectFinished() {
			addBuildable(project, projectPosition.x, projectPosition.y);
			
			homeMap.removeTileObject(mark);
		}

		private void addProduction() {
			complete += (float) productionIncome / (float) project.getBaseCost();
		}

		public final BuildableByCity getProject() {
			return project;
		}

		public final float getComplete() {
			return complete;
		}

	}

	@Override
	public boolean staticVisibility() {
		return true;
	}
	
	@Override
	public <T extends Movable & TileObject> boolean isWalkable(T obj) {
		return true;
	}

}
