/**
 * 
 */
package game_logic.gui.cardsManager;

import org.joml.Vector2f;

import graphicsSupport.gui.VgGui;
import graphicsSupport.gui.components.VgButton;

/**
 * @author timat
 *
 */
public class CardsManager implements VgGui {

	private Panel panel = new Panel();
	private VgButton hide = new VgButton(new Vector2f(0.75f, 0.32f), new Vector2f(0.8f, 0.39f), 0f, ">") {

		@Override
		protected void clicked_left() {
			show = !show;
			hide.move(show ? -0.2f : 0.2f, 0f);
		}

	};

	private boolean show = false;

	public CardsManager() {
		hide.move(show ? -0.2f : 0.2f, 0f);
	}

	@Override
	public void update() throws InterruptInput {
		hide.update();
		if (!show) {
			return;
		}
		panel.update();
	}

	@Override
	public boolean contains(Vector2f vec) {
		if (!show) {
			return false;
		}
		return panel.isInComponent(vec) || hide.isInComponent(vec);
	}

	@Override
	public void render() {
		hide.render();
		if (!show) {
			return;
		}
		panel.render();

	}

}
