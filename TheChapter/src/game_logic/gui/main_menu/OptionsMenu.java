package game_logic.gui.main_menu;

import org.joml.Vector2f;

import graphicsSupport.gui.VgGui;
import graphicsSupport.gui.VgGuiHandler;
import graphicsSupport.gui.components.VgButton;
import graphicsSupport.gui.components.VgPanel;

public class OptionsMenu implements VgGui {

	private VgPanel backGround = new VgPanel(new Vector2f(0, 0), new Vector2f(1, 1));
	private VgButton back_to_menu = new VgButton(new Vector2f(0.05f, 0.85f), new Vector2f(0.15f, 0.9f), 0, "Back") {

		@Override
		protected void clicked_left() {
			VgGuiHandler.instance.removeHUD(OptionsMenu.this);
			VgGuiHandler.instance.addHUD(new MainMenu());
		}

	};

	public OptionsMenu() {
		back_to_menu.setIndentation(0.03f);
		back_to_menu.setIndentationY(0.027f);
		back_to_menu.setTextHeight(0.05f);
	}

	@Override
	public void update() {
		back_to_menu.update();
	}

	@Override
	public boolean contains(Vector2f vec) {
		return back_to_menu.isInComponent(vec);
	}

	@Override
	public void render() {
		backGround.render();
		back_to_menu.render();
	}

}
