	package scenario;

import java.io.File;

import engine.loaders.Loader;
import game_logic.map.MapGenerator;
import game_logic.map.scenario.Scenario;
import game_logic.map.scenario.ScenarioProperties;
import resources.ResourcesPack;
import scenario.storage.Resources;
import scenario.stuf.Forest;
import scenario.stuf.House;
import scenario.stuf.LineInfantry;

public class TestScenario extends Scenario {

	private ResourcesPack[] resources = new ResourcesPack[4];

	public TestScenario() {
	}

	@Override
	protected void load() {
		addResourceType(Resources.WOOD);
		addResourceType(Resources.FOOD);

		addTileObjectType(LineInfantry.class);
		addTileObjectType(Forest.class);
		
		addTileObjectType(House.class);
		
		
	}

	@Override
	protected MapGenerator createGenerator() {
		return new TestMapGenerator();
	}

	@Override
	public void loadResources() throws Exception {
		resources[0] = Loader.loadModels(new File(this.getLocalResourcesPath() + "/models"));
		resources[1] = Loader.loadAnimations(new File(this.getLocalResourcesPath() + "/animations"));
		resources[2] = Loader.loadSounds(new File(this.getLocalResourcesPath() + "/sounds"));
		resources[3] = Loader.loadTextures(new File(this.getLocalResourcesPath() + "/textures"));
		for (int i = 0; i < resources.length; i++) {
			resources[i].seal();
		}
	}

	@Override
	public void cleanUp() throws Exception {
		ResourcesPack.cleanupPacks(resources);
	}

	@Override
	protected ScenarioProperties createProperties() {
		return new TestProperties();
	}

}
