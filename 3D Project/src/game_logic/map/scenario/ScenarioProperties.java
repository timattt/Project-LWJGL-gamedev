package game_logic.map.scenario;

import game_logic.map.resources.Resource;
import game_logic.map.settlement.Building;
import game_logic.map.settlement.BuildingProcessMark;

public abstract class ScenarioProperties {

	public ScenarioProperties() {
	}

	public abstract Class<? extends Building> getCityMainBuilding();

	public abstract Resource getCitiesConstructionalResource();
	
	public abstract Resource getCitiesLivelihoodResource();

	public abstract Class<? extends BuildingProcessMark> getCitiesBuildingProcessMarker();
	
}
