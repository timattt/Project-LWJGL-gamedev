package scenario;

import game_logic.map.resources.Resource;
import game_logic.map.scenario.ScenarioProperties;
import game_logic.map.settlement.Building;
import game_logic.map.settlement.BuildingProcessMark;
import graphicsSupport.mesh.Mesh;
import scenario.storage.Meshes;
import scenario.storage.Resources;
import scenario.stuf.House;

public class TestProperties extends ScenarioProperties {

	public TestProperties() {
	}

	@Override
	public Class<? extends Building> getCityMainBuilding() {
		return House.class;
	}

	@Override
	public Resource getCitiesConstructionalResource() {
		return Resources.TOOLS;
	}

	@Override
	public Resource getCitiesLivelihoodResource() {
		return Resources.FOOD;
	}

	@Override
	public Class<? extends BuildingProcessMark> getCitiesBuildingProcessMarker() {
		return CityMarker.class;
	}

	public static class CityMarker extends BuildingProcessMark {

		public CityMarker() {
			super(new Mesh[][] { Meshes.BUILDING_MARK }, 10f);
		}

	}

}
