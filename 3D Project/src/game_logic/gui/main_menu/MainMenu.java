package game_logic.gui.main_menu;

import java.util.LinkedList;

import org.joml.Vector2f;

import engine.Engine;
import game_logic.Executor;
import game_logic.GameProcess;
import game_logic.gui.events.Delegate;
import game_logic.gui.events.EventsCreator;
import game_logic.map.Map;
import game_logic.map.scenario.Scenario;
import game_logic.map.scenario.ScenarioLoader;
import graphicsSupport.gui.VgGui;
import graphicsSupport.gui.VgGuiHandler;
import graphicsSupport.gui.components.VgButton;
import graphicsSupport.gui.components.VgPanel;

public class MainMenu implements VgGui, EventsCreator {

	// Components
	private VgPanel backGround = new VgPanel(new Vector2f(0, 0), new Vector2f(1, 1));

	private VgButton play = new VgButton(new Vector2f(0.4f, 0.1f), new Vector2f(0.6f, 0.2f), 0.049f, "Play") {

		@Override
		protected void clicked_left() {
			Executor init = new Executor() {
				@Override
				public void executed() {
					ScenarioLoader loader = new ScenarioLoader(
							Engine.getResourceAsFile("/scenarios/TestScenario/TestScenario.jar"));
					Scenario scenario = loader.process();
					Map map = null;
					try {
						map = new Map(scenario);
					} catch (Exception e) {
						e.printStackTrace();
					}
					GameProcess.instance.setMap(map, 0);
				}
			};
			Executor.execute(init);

			VgGuiHandler.instance.removeHUD(MainMenu.this);
		}

	};
	private VgButton options = new VgButton(new Vector2f(0.4f, 0.25f), new Vector2f(0.6f, 0.35f), 0.049f, "Options") {

		@Override
		protected void clicked_left() {
			VgGuiHandler.instance.removeHUD(MainMenu.this);
			VgGuiHandler.instance.addHUD(new OptionsMenu());
		}

	};
	private VgButton exit = new VgButton(new Vector2f(0.4f, 0.4f), new Vector2f(0.6f, 0.5f), 0.049f, "Exit") {

		@Override
		protected void clicked_left() {
			Engine.stop();
		}

	};
	private VgButton wb = new VgButton(new Vector2f(0.4f, 0.55f), new Vector2f(0.65f, 0.65f), 0.049f, "Map builder") {

		@Override
		protected void clicked_left() {
		}

	};

	public MainMenu() {
		backGround.setR(100);
		backGround.setG(100);
		backGround.setB(100);
		wb.setIndentation(0.075f);
	}

	@Override
	public void prepare(LinkedList<Delegate> list) {
	}

	@Override
	public void update() {
		wb.update();
		play.update();
		options.update();
		exit.update();
	}

	@Override
	public void render() {
		backGround.render();
		play.render();
		options.render();
		exit.render();
		wb.render();
	}

	@Override
	public boolean contains(Vector2f vec) {
		return play.isInComponent(vec) || options.isInComponent(vec) || exit.isInComponent(vec)
				|| wb.isInComponent(vec);
	}

}
