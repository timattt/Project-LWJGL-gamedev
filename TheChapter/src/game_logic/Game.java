package game_logic;

import engine.Engine;
import engine.loaders.Loader;
import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInit;
import engine.monoDemeanor.MonoDemeanorInstance;
import engine.monoDemeanor.MonoDemeanorPriority;
import engine.monoDemeanor.MonoDemeanorUpdate;
import game_logic.graphics.StrategyCamera;
import game_logic.gui.main_menu.MainMenu;
import graphicsSupport.camera.Camera;
import graphicsSupport.gui.VgGuiHandler;

@MonoDemeanor
public class Game {

	@MonoDemeanorInstance
	public static final Game instance = new Game();
	
	private Game() {
	}

	@MonoDemeanorInit(priority = MonoDemeanorPriority.MEDIUM)
	public void init() throws Exception {
		Loader.loadModels(Engine.getResourceAsFile("/models"));
		Loader.loadAnimations(Engine.getResourceAsFile("/animations"));
		
		Loader.loadTextures(Engine.getResourceAsFile("/textures"));
		Loader.loadSounds(Engine.getResourceAsFile("/sounds"));
		
		Camera.CURRENT_CAMERA = StrategyCamera.instance;
		VgGuiHandler.instance.addHUD(new MainMenu());
	}

	@MonoDemeanorUpdate
	public void update() {
		Executor.updateExecutor();
	}

}
