package game_logic.gui;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import engine.Window;
import game_logic.GameProcess;
import game_logic.gui.main_menu.MainMenu;
import graphicsSupport.gui.VgGui;
import graphicsSupport.gui.VgGuiHandler;
import graphicsSupport.gui.components.VgButton;
import graphicsSupport.gui.components.VgPanel;

public class InGameMenu implements VgGui {

	// Show
	private boolean showMenu = false;

	// Panels
	private VgPanel menu = new VgPanel(new Vector2f(0, 0), new Vector2f(0.2f, 0.4f));
	private VgButton exit = new VgButton(new Vector2f(0f, 0f), new Vector2f(0.2f, 0.1f), 0.05f, "Exit") {

		@Override
		protected void clicked_left() {
			VgGuiHandler.instance.addHUD(new MainMenu());
			GameProcess.instance.deleteMap();
		}

	};

	private long startPressTime;

	public InGameMenu() {
		exit.setR(200);
	}

	@Override
	public void update() throws InterruptInput {
		if (GLFW.glfwGetKey(Window.instance.getWindowID(), GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
			if (startPressTime == -1) {
				startPressTime = System.currentTimeMillis();
			}
			if (System.currentTimeMillis() - startPressTime > 10 && startPressTime != -2) {
				showMenu = !showMenu;
				startPressTime = -2;
			}
			VgGui.interruptInput();
		} else {
			startPressTime = -1;
		}
		if (!showMenu) {
			return;
		}

		exit.update();
	}

	@Override
	public boolean contains(Vector2f vec) {
		return showMenu ? (menu.isInComponent(vec)) : false;
	}

	@Override
	public void render() {
		if (!showMenu) {
			return;
		}
		menu.render();
		exit.render();
	}

}
